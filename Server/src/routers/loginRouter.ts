import express from 'express'
import { LoginController } from '../controllers/LoginController'
import multer from 'multer';
import { JWT } from '../JWT';

const upload = multer({
    storage: multer.memoryStorage(),
    limits: {
        fileSize: 2*1024*1024,
        files: 1
    }
});

const loginRouter = express.Router()

loginRouter.route("/test").get((req: express.Request, res: express.Response, next: express.NextFunction) => {
    new LoginController().test(req, res).catch(next)
})
loginRouter.route("/login").post((req: express.Request, res: express.Response, next: express.NextFunction) => {
    new LoginController().login(req, res).catch(next)
})
loginRouter.route("/refresh").get(JWT.authenticateJWT, (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new LoginController().refresh(req, res).catch(next)
})
loginRouter.route("/register").post(upload.single("profile"), (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new LoginController().register(req, res).catch(next)
})


export default loginRouter