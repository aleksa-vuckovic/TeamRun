import express from 'express'
import multer from 'multer'
import { JWT } from '../JWT'
import { UserController } from '../controllers/UserController'

const upload = multer({
    storage: multer.memoryStorage(),
    limits: {
        fileSize: 2*1024*1024,
        files: 1
    }
});

const userRouter = express.Router()

userRouter.use(JWT.authenticateJWT)

userRouter.route("/data").get((req: express.Request, res: express.Response, next: express.NextFunction) => {
    new UserController().data(req, res).catch(next)
})
userRouter.route("/update").post(upload.single('profile'), (req: express.Request, res: express.Response, next: express.NextFunction) => {
    new UserController().update(req, res).catch(next)
})

export default userRouter