import userModel, { User } from './models/User'
import runModel, { PathPoint, Run, RunInner, RunProjection } from './models/Run'
import roomModel, { Room } from './models/Room'
import { ObjectId } from 'mongodb'
import eventModel, { Event, EventRanking } from './models/Event'

type EventProjection = Omit<Event, "followers"> & {followers: number, following: boolean}

export class DB {

    static async userByMail(email: string): Promise<User | null> {
        return (await userModel.findOne({email: email}))?.toObject() ?? null
    }

    static async userById(id: ObjectId): Promise<User | null> {
        return (await userModel.findOne({_id: id}))?.toObject() ?? null
    }

    static async addUser(user: User): Promise<void> {
        userModel.insertMany([user])
    }
    static async updateUser(_id: ObjectId, data: Partial<User>): Promise<void> {
        let ret = await userModel.updateOne({_id: _id}, {$set: data})
        if (ret.modifiedCount == 0) throw "Database error."
    }
    static async run(user: ObjectId, id: number): Promise<Omit<Run, "path"> | null> {
        //TO DO -> return the corresponding run object NO path
        let ret: Run[] = await runModel.aggregate([
            {
                $match: { user: user, id: id}
            },
            {
                $unset: "path"
            }
        ])
        if (ret.length > 0) return ret[0]
        else return null
    }

    static async createRun(run: RunInner): Promise<void> {
        await runModel.insertMany([run])
    }

    static async updateRun(user: ObjectId, id: number, data: Partial<Run>): Promise<void> {
        let ret = await runModel.updateOne({user: user, id: id}, {$set: data})
        if (ret.modifiedCount == 0) throw "Database error."
    }

    static async updatePath(user: ObjectId, id: number, path: PathPoint[]): Promise<void> {
        let ret = await runModel.updateOne({user: user, id: id}, {$push: {path: {$each: path}}})
        if (ret.modifiedCount == 0) throw "Database error."
    }

    private static runProjection = {
        id: "$id",
        user: "$user",
        room: "$room",
        event: "$event",
        start: "$start",
        running: "$running",
        end: "$end",
        paused: "$paused",
        cur: "$cur"
    }
    static async getRunUpdate(match: {user: ObjectId, id?: number, room?: ObjectId, event?: ObjectId}, since: number): Promise<Run | null> {
        let ret = await runModel.aggregate([
            {
                $match: match
            },
            {
                $project: {
                    _id: 0,
                    run: DB.runProjection,
                    location: "$location",
                    path: {
                        $filter: {
                            input: "$path",
                            cond: {$gt: ["$$this.time", since]}
                        }
                    }
                }
            }
        ])
        if (ret.length > 0) return ret[0]
        else return null
    }

    static async createRoom(): Promise<ObjectId> {
        let room = new roomModel({
            members: [],
            ready: [],
            start: null
        })
        let saved = await room.save()
        return saved._id
    }

    static async joinRoom(user: ObjectId, room: ObjectId): Promise<void> {
        let ret = await roomModel.updateOne({_id: room}, {$addToSet: {members: user}})
        if (ret.matchedCount == 0) throw"Room does not exist."
    }

    static async readyRoom(user: ObjectId, room: ObjectId): Promise<void> {
        let ret = await roomModel.updateOne({_id: room, members: user}, {$addToSet: {ready: user}})
        if (ret.matchedCount == 0) throw "Room does not exist, or the user is not a member."
    }

    static async leaveRoom(user: ObjectId, room: ObjectId): Promise<void> {
        let ret = await roomModel.updateOne({_id: room}, {$pull: {members: user, ready: user}})
        if (ret.matchedCount == 0) throw "Room does not exist."
    }

    static async room(id: ObjectId): Promise<Room | null> {
        return (await roomModel.findOne({_id: id}))?.toObject() ?? null
    }
    static async startRoom(id: ObjectId): Promise<void> {
        let ret = await roomModel.updateOne({_id: id}, {$set: {start: Date.now()}})
        if (ret.modifiedCount == 0) throw "Database error."
    }

    private static eventProjection(user: ObjectId | null): any {
        return {
            $project: {
                name: "$name",
                description: "$description",
                image: "$image",
                time: "$time",
                distance: "$distance",
                path: "$path",
                tolerance: "$tolerance",
                followers: {
                    $size: "$followers"
                },
                following: user == null ? false : {
                    $in: [user, "$followers"]
                }
            }
        }
    }
    static async createEvent(event: Event): Promise<ObjectId> {
        let ret = await eventModel.insertMany([event])
        return ret[0]._id
    }
    
    static async event(id: ObjectId, user: ObjectId | null): Promise<EventProjection | null> {
        let ret = await eventModel.aggregate([
            {$match: {_id: id}},
            this.eventProjection(user),
        ])
        if (ret.length > 0) return ret[0]
        else return null
    }

    static async findEvent(user: ObjectId | null, search: string | null, following: boolean | null): Promise<Array<EventProjection>> {
        let pipeline = [DB.eventProjection(user)]
        if (search != null) pipeline.push(
            {
                $match: {
                    $or: [
                        { name: {$regex: new RegExp(search, "i")} },
                        { description: {$regex: new RegExp(search, "i")}}
                    ],
                }
            }
        )
        if (following != null) pipeline.push(
            {
                $match: {following: following}
            }
        )
        pipeline.push(
            {
                $match: {time: {$gt: Date.now() - 60*60*1000}}
            }
        )
        pipeline.push(
            {
                $sort: {time: 1}
            }
        )
        pipeline.push(
            {
                $limit: 10
            }
        )
        let ret = await eventModel.aggregate(pipeline)
        return ret
    }

    static async followEvent(user: ObjectId, event: ObjectId): Promise<void> {
        let ret = await eventModel.updateOne({_id: event}, {$addToSet: {followers: user}})
        if (ret.matchedCount == 0) throw "Event does not exist."
    }

    static async unfollowEvent(user: ObjectId, event: ObjectId): Promise<void> {
        let ret = await eventModel.updateOne({_id: event}, {$pull: {followers: user}})
        if (ret.matchedCount == 0) throw "Event does not exist."
    }

    static async getRuns(user: ObjectId, until: number, limit: number): Promise<RunProjection[]> {
        let ret = await runModel.aggregate([
            {
                $match: {
                    user: user,
                    end: {$ne: null},
                    start: {$lt: until}
                }
            },
            {
                $project: {
                    run: DB.runProjection,
                    location: "$location",
                    path: []
                }
            },
            {
                $sort: {"run.start": -1}
            },
            {
                $limit: limit
            }
        ])
        return ret
    }

    static async getRunsSince(user: ObjectId, since: number): Promise<RunProjection[]> {
        let ret = await runModel.aggregate([
            {
                $match: {
                    user: user,
                    end: {$ne: null},
                    start: {$gte: since}
                }
            },
            {
                $project: {
                    run: DB.runProjection,
                    location: "$location",
                    path: []
                }
            },
            {
                $sort: {"run.start": 1}
            }
        ])
        return ret
    }

    static async unfinishedRun(user: ObjectId): Promise<RunProjection | null> {
        let ret = await runModel.aggregate([
            {
                $match: {
                    user: user,
                    end: {$eq: null}
                }
            },
            {
                $sort: {"start": -1}
            },
            {
                $limit: 1
            },
            {
                $project: {
                    _id: 0,
                    run: DB.runProjection,
                    location: "$location",
                    path: []
                }
            }
        ])
        if (ret.length > 0) return ret[0]
        else return null
    }


    static async deleteRun(user: ObjectId, runId: number): Promise<void> {
        await runModel.deleteOne({user: user, id: runId})
    }

    static async eventRanking(eventID: ObjectId, includeDQ: boolean = true): Promise<Array<EventRanking>> {
        let event = await eventModel.findOne({_id: eventID})
        if (event == null) return []
        let pipeline: any[] = [
            {
                $match: {
                    event: event._id,
                    end: {$ne: null}
                }
            },
            {
                $project: {
                    _id: 0,
                    user: "$user",
                    time: {$subtract: ["$end", event.time]},
                    finish: {
                        $arrayElemAt: ["$path", -1]
                    },
                }
            },
            {
                $lookup: {
                    from: "users",
                    localField: "user",
                    foreignField: "_id",
                    as: "data"
                }
            },
            {
                $unwind: "$data"
            },
            {
                $project: {
                    user: "$user",
                    name: "$data.name",
                    last: "$data.last",
                    time: "$time",
                    disqualified: {"$not": "$finish.end"}
                }
            },
            {
                $sort: {
                    disqualified: 1,
                    time: 1
                }
            }
        ]
        if (!includeDQ) pipeline.push({$match: {"disqualified": false}})
        return await runModel.aggregate(pipeline)
    }
    static async eventRankingLive(eventID: ObjectId): Promise<Array<EventRanking>> {
        let ranking = await this.eventRanking(eventID, false)
        if (ranking.length >= 10) return ranking.slice(0, 10)
        let event = await eventModel.findOne({_id: eventID})
        if (event == null) return []
        let ret = await runModel.aggregate([
            {
                $match: {
                    event: eventID,
                    end: {$eq: null}
                }
            },
            {
                $project: {
                    _id: 0,
                    user: "$user",
                    distance: "$location.distance"
                }
            },
            {
                $lookup: {
                    from: "users",
                    localField: "user",
                    foreignField: "_id",
                    as: "data"
                }
            },
            {
                $unwind: "$data"
            },
            {
                $project: {
                    user: "$user",
                    name: "$data.name",
                    last: "$data.last",
                    distance: "$distance"
                }
            },
            {
                $sort: {"distance": -1}
            },
            {
                $limit: 10-ranking.length
            }
        ])
        return ranking.concat(ret)   
    }
}