import express from 'express'
import { Validation } from '../Validations'
import { DB } from '../DB'
import { ObjectId } from 'mongodb'
import { Run } from '../models/Run'

export class RunController {

    create = async (req: express.Request, res: express.Response) => {
        let input = req.body
        let output: Run = await Validation.run(input, req.jwt!)
        let existing = await DB.run(output.user, output.id)
        if (existing != null)
            res.json({message: "ok"})
        await DB.createRun(output)
        res.json({message: "ok"})
    }

    update = async (req: express.Request, res: express.Response) => {
        let input = req.body
        let output: Partial<Run> = Validation.runUpdate(input, req.jwt!)
        let path = output.path ?? []
        delete output.path
        await DB.updateRun(output.user!, output.id!, output)
        if (path.length > 0) await DB.updatePath(output.user!, output.id!, path)
        res.json({message: "ok"})
    }

    getUpdate = async (req: express.Request, res: express.Response) => {
        if (!req.query.user)
            throw "Not enough data."
        let match: {user: ObjectId, id?: number, room?: ObjectId, event?: ObjectId} = {user: new ObjectId(String(req.query.user))}
        if (req.query.id) {
            let id = parseInt(req.query.id as string)
            if (isNaN(id))
                throw "Invalid ID."
            match.id = id
        }
        else if (req.query.room) match.room = new ObjectId(req.query.room as string)
        else if (req.query.event) match.event = new ObjectId(req.query.event as string)
        else throw "Not enough data."
        let since = parseInt(req.query.since as string)
        if (isNaN(since)) since = 0
        let ret = await DB.getRunUpdate(match, since)
        if (ret == null) throw "Run does not exist."
        res.json({message: "ok", data: ret})
    }

    all = async(req: express.Request, res: express.Response) => {
        let until = parseInt(req.query.until as string)
        let limit = parseInt(req.query.limit as string)
        if (isNaN(until) || isNaN(limit))
            throw "Invalid query parameter data."
        let ret = await DB.getRuns(req.jwt!._id, until, limit)
        res.json({message: "ok", data: ret})
    }

    since = async(req: express.Request, res: express.Response) => {
        let since = parseInt(req.query.since as string)
        if (isNaN(since))
            throw "Invalid query parameter data."
        let ret = await DB.getRunsSince(req.jwt!._id, since)
        res.json({message: "ok", data: ret})
        return 
    }

    unfinished = async(req: express.Request, res: express.Response) => {
        let ret = await DB.unfinishedRun(req.jwt!._id)
        res.json({message: "ok", data: ret})
        return 
    }

    delete = async(req: express.Request, res: express.Response) => {
        let runId = parseInt(req.params.id)
        if (isNaN(runId))
            throw "Invalid run id."
        await DB.deleteRun(req.jwt!._id, runId)
        res.json({message: "ok"})
    }
}