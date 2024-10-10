import express from 'express'
import { Validation } from '../Validations'
import { Utils } from '../Utils'
import fs from 'fs'
import { DB } from '../DB'
import { Event } from '../models/Event'
import { ObjectId } from 'mongodb'


var liveRankingSubscribers: {[key: string]: Array<express.Response>} = {}

setInterval(async () => {
    for (const eventID in liveRankingSubscribers) {
        if (!liveRankingSubscribers.hasOwnProperty(eventID)) continue
        let ranking: any = await DB.eventRankingLive(new ObjectId(eventID))
        ranking = JSON.stringify(ranking)
        ranking = `data: ${ranking}\n\n`
        for (const sub of liveRankingSubscribers[eventID]) {
            const res = sub
            res.write(ranking)
        }
    }
}, 5000)

export class EventController {

    create = async(req: express.Request, res: express.Response) => {
        let input = req.body
        let output: Event = Validation.event(input, req.jwt!)
        let image: any = req.file
        Validation.profile(image)
        let uniqueName = `${Utils.randomUniqueFileName()}.${image.mimetype.split('/')[1]}`
        let uploadPath = Utils.uploadPath(uniqueName)
        fs.writeFileSync(uploadPath, image.buffer)
        output.image = uniqueName
        
        let ret = await DB.createEvent(output)
        res.json({message: "ok", data: ret})
        return
    }
    
    data = async(req: express.Request, res: express.Response) => {
        let ret = await DB.event(new ObjectId(req.params.event), new ObjectId(req.jwt!._id))
        if (ret == null) throw "Event not found."
        res.json({message: "ok", data: ret})
    }
    find = async(req: express.Request, res: express.Response) => {
        let following = Utils.parseBoolean(req.query.following)
        let search = req.query.search ? req.query.search as string : null
        let ret = await DB.findEvent(new ObjectId(req.jwt!._id), search, following)
        res.json({message: "ok", data: ret})
    }
    follow = async(req: express.Request, res: express.Response) => {
        await DB.followEvent(new ObjectId(req.jwt!._id), new ObjectId(req.params.event))
        res.json({message: "ok"})
    }
    unfollow = async(req: express.Request, res: express.Response) => {
        await DB.unfollowEvent(new ObjectId(req.jwt!._id), new ObjectId(req.params.event))
        res.json({message: "ok"})
    }

    ranking = async(req: express.Request, res: express.Response) => {
        let eventID = req.params.event
        let ret = await DB.eventRanking(new ObjectId(eventID))
        res.json({message: "ok", data: ret})
    }

    rankingLive = async(req: express.Request, res: express.Response) => {
        let eventID = req.params.event
        let ret = await DB.eventRankingLive(new ObjectId(eventID))
        res.json({message: "ok", data: ret})
    }

    rankingSubscribe = async(req: express.Request, res: express.Response) => {
        let eventID = req.params.event
        if (!liveRankingSubscribers[eventID]) liveRankingSubscribers[eventID] = []
        liveRankingSubscribers[eventID].push(res)
        const headers = {
            'Content-Type': 'text/event-stream',
            'Connection': 'keep-alive',
            'Cache-Control': 'no-cache'
          };
        res.writeHead(200, headers)
        res.write("data: Initial data\n\n")
        req.once('close', () => {
            liveRankingSubscribers[eventID] = liveRankingSubscribers[eventID].filter((elem : any) => elem != res)
            if (liveRankingSubscribers[eventID].length == 0) delete liveRankingSubscribers[eventID]
        })
    }
}