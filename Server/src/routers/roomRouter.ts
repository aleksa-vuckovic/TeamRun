import express from 'express'
import { JWT } from '../JWT'
import { RoomController } from '../controllers/RoomController'
import Room from '../models/Room'

const roomRouter = express.Router()

roomRouter.use(JWT.authenticateJWT)

roomRouter.route("/create").get(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new RoomController().create(req, res).catch(next)
})
roomRouter.route("/join/:room").get(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new RoomController().join(req, res).catch(next)
})
roomRouter.route("/ready/:room").get(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new RoomController().ready(req, res).catch(next)
})
roomRouter.route("/leave/:room").get(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new RoomController().leave(req, res).catch(next)
})
roomRouter.route("/status/:room").get(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new RoomController().status(req, res).catch(next)
})

export default roomRouter