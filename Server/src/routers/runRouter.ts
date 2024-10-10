import express from 'express'
import { JWT } from '../JWT'
import { RunController } from '../controllers/RunController'

const runRouter = express.Router()

runRouter.use(JWT.authenticateJWT)

runRouter.route("/create").post(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new RunController().create(req, res).catch(next)
})
runRouter.route("/update").post(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new RunController().update(req, res).catch(next)
})
runRouter.route("/getupdate").get(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new RunController().getUpdate(req, res).catch(next)
})
runRouter.route("/all").get(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new RunController().all(req, res).catch(next)
})
runRouter.route("/since").get(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new RunController().since(req, res).catch(next)
})
runRouter.route("/unfinished").get(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new RunController().unfinished(req, res).catch(next)
})
runRouter.route("/delete/:id").get(async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new RunController().delete(req, res).catch(next)
})

export default runRouter