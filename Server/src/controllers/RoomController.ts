import express from 'express'
import { DB } from '../DB'
import { ObjectId } from 'mongodb'

export class RoomController {

    create =  async (req: express.Request, res: express.Response) => {
        let room = await DB.createRoom()
        await DB.joinRoom(req.jwt!._id, room)
        res.json({message: "ok", data: room})
    }

    join = async (req: express.Request, res: express.Response) => {
        let room = new ObjectId(req.params.room)
        let state = await DB.room(room)
        if (state == null)
            throw "Room does not exist."
        if (state.start != null)
            throw "The run has already started."
        if (state.members.length >= 5)
            throw "The room is full (5 users max)."
        await DB.joinRoom(req.jwt!._id, room)
        res.json({message: "ok"})
    }

    ready = async (req: express.Request, res: express.Response) => {
        let room = new ObjectId(req.params.room)
        await DB.readyRoom(req.jwt!._id, room)
        let status = await DB.room(room)
        if (status != null && status.members.length == status.ready.length && status.start == null)
            await DB.startRoom(room)
        res.json({message: "ok"})
    }

    leave = async (req: express.Request, res: express.Response) => {
        let user = req.jwt!._id
        let room = new ObjectId(req.params.room)
        let state = await DB.room(room)
        if (state == null)
            throw "Room does not exist."
        if (state.ready.find(id => id.equals(user)))
            throw "Cannot leave when ready."
        await DB.leaveRoom(user, room)
        res.json({message: "ok"})
        return
    }

    status = async (req: express.Request, res: express.Response) => {
        let room = new ObjectId(req.params.room)
        let ret = await DB.room(room)
        if (ret == null) throw "Room does not exist"
        res.json({message: "ok", data: ret})
    }
}