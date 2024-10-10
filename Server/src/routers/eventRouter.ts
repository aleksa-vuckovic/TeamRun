import express from 'express'
import { EventController } from '../controllers/EventController'
import { JWT } from '../JWT'
import multer from 'multer';

const upload = multer({
    storage: multer.memoryStorage(),
    limits: {
        fileSize: 2*1024*1024,
        files: 1
    }
});

const eventRouter = express.Router()

eventRouter.use(JWT.authenticateJWT)

eventRouter.route("/create").post(upload.single("image"), async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new EventController().create(req, res).catch(next)
})
eventRouter.route("/data/:event").get(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new EventController().data(req, res).catch(next)
})
eventRouter.route("/find").get(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new EventController().find(req, res).catch(next)
})
eventRouter.route("/follow/:event").get(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new EventController().follow(req, res).catch(next)
})
eventRouter.route("/unfollow/:event").get(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new EventController().unfollow(req, res).catch(next)
})
eventRouter.route("/ranking/:event").get(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new EventController().ranking(req, res).catch(next)
})
eventRouter.route("/rankinglive/:event").get(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new EventController().rankingLive(req, res).catch(next)
})
eventRouter.route("/rankingsubscribe/:event").get(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new EventController().rankingSubscribe(req, res).catch(next)
})

export default eventRouter