import { ObjectId } from "mongodb"
import { DB } from "./DB"
import { User, UserPayload } from "./models/User"
import { PathPoint, Run, RunInner, RunProjection } from "./models/Run"
import { Event } from "./models/Event"


export class Validation {


    static async registration(input: any): Promise<User> {
        if (!input || !input.email || !input.password || !input.name || !input.last || !input.weight) throw "Not enough data."
        let existingUser = await DB.userByMail(input.email)
        if (existingUser != null) throw "The email is already in use."
        let weight = parseFloat(input.weight)
        if (isNaN(weight)) throw "Weight must be a number."
        return {
            email : input.email,
            password : input.password,
            name : input.name,
            last : input.last,
            weight : weight,
            profile: ""
        }
    }

    static profile(file: any): void {
        if (!file) throw "No file."
        let type = file.mimetype
        if (type != "image/png") throw "Unsupported image type."
    }

    static userUpdate(input: any): Partial<User> {
        let output: Partial<User> = {}
        if (input.name) output.name = input.name
        if (input.last) output.last = input.last
        if (input.weight) {
            let weight = parseFloat(input.weight)
            if (isNaN(weight)) throw "Weight must be a number."
            output.weight = weight
        }
        return output
    }

    static async run(input: any, jwt: UserPayload): Promise<Run> {
        if (!input || !input.user || !input.id) throw "Not enough data."
        let user: ObjectId = new ObjectId(input.user)
        if (!jwt._id.equals(user)) throw "Cannot create a run for someone else."
        let id = parseInt(input.id)
        if (isNaN(id)) throw "Run id must be an integer."
        let run = await DB.run(user, id)
        if (run != null) throw "Run already exists."
        
        let start: number | null = null
        if (input.start) {
            start = parseInt(input.start)
            if (isNaN(start)) throw "Start time must be an integer."
        }
        let running: number = 0
        if (input.running) {
            running = parseInt(input.running)
            if (isNaN(running)) throw "Running time must be an integer."
        }
        let end: number | null = null
        if (input.end) {
            end = parseInt(input.end)
            if (isNaN(end)) throw "End time must be an integer."
        } else end = null
        let paused = false
        if (input.paused && input.paused != "false") paused = true
       
        let event: ObjectId | null = null
        if (input.event)
            //TO DO check that the event exists
            event = new ObjectId(input.event)
        let room: ObjectId | null = null
        if (input.room)
            //TO DO check that the user joined the room
            room = new ObjectId(input.room)

        return {
            id: id,
            paused: paused,
            running: running,
            cur: null,
            penalty: null,
            event: event,
            room: room,
            start: start,
            end: end,
            user: user,
            location: {
                latitude: 0,
                longitude: 0,
                altitude: 0,
                time: 0,
                end: false,
                speed: 0,
                distance: 0,
                kcal: 0
            },
            path: []
        }
    }

    private static runLocation(input: any): PathPoint {
        if (!input) throw "Not enough data."
        let output = {
            latitude: parseFloat(input.latitude),
            longitude: parseFloat(input.longitude),
            altitude: parseFloat(input.altitude),
            time: parseInt(input.time),
            end: (input.end  && input.end != "false") ? true : false,
            speed: parseFloat(input.speed),
            distance: parseFloat(input.distance),
            kcal: parseFloat(input.kcal),
        }
        

        if (isNaN(output.latitude) || isNaN(output.longitude)
            || isNaN(output.altitude) || isNaN(output.time)
            || isNaN(output.speed) || isNaN(output.distance)
            || isNaN(output.kcal)) throw "Incorrect number format."
        return output
    }

    static runUpdate(input: any, jwt: UserPayload): Partial<Run> {
        if (!input) throw "Not enough data."
        let run = input.run
        let output: Partial<Run> = {}

        if (!run.user || !run.id) throw "Not enough data."
        output.user = new ObjectId(run.user)
        if (!jwt._id.equals(output.user)) throw "Cannot update a run for someone else."
        output.id = parseInt(run.id)
        if (isNaN(output.id)) throw "Run id must be an integer."
        if (run.start) {
            output.start = parseInt(run.start)
            if (isNaN(output.start)) throw "Start time must be an integer."
        }
        if (run.running) {
            output.running = parseInt(run.running)
            if (isNaN(output.running)) throw "Running time must be an integer."
        }
        if (run.end) {
            output.end = parseInt(run.end)
            if (isNaN(output.end)) throw "End time must be an integer."
        }
        if (run.paused === true || run.paused === "true") output.paused = true
        else if (run.paused === false || run.paused === "false") output.paused = false
        if (run.cur) {
            output.cur = parseInt(run.cur)
            if (isNaN(output.cur)) throw "Current point must be an integer index."
        }
        if (run.penalty) {
            output.penalty = parseFloat(run.penalty)
            if (isNaN(output.penalty)) throw "Penalty must be a number."
        }
        

        if (input.location)
            output.location = Validation.runLocation(input.location)

        output.path = []
        if (input.path)
            input.path.forEach((pathPoint: any) => {
                output.path!.push(Validation.runLocation(pathPoint))
            });
        
        return output
    }

    static event(input: any, jwt: UserPayload): Event {
        if (!input || !input.name || !input.time || !input.distance) throw "Name, time and distance must not be empty."
        let name = input.name
        let time = parseInt(input.time)
        if (isNaN(time)) throw "Invalid time format."
        if (time < Date.now()) throw "The event start must be in the future."
        let distance = parseFloat(input.distance)
        if (isNaN(distance)) throw "Invalid distance format."
        let description = input.description ? input.description : ""
        let followers = [new ObjectId(jwt._id)]
        let path = input.path ? JSON.parse(input.path) : null
        let tolerance = null
        if(path) {
            tolerance = parseFloat(input.tolerance)
            if (isNaN(tolerance)) throw "Invalid tolerance value."
        }
        
        return {
            name: name,
            time: time,
            distance: distance,
            description: description,
            followers: followers,
            path: path,
            tolerance: tolerance,
            image: ""
        }
    }
}