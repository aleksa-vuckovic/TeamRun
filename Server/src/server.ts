import express from 'express'
import mongoose from 'mongoose'
import loginRouter from './routers/loginRouter'
import userRouter from './routers/userRouter'
import path from 'path'
import runRouter from './routers/runRouter'
import roomRouter from './routers/roomRouter'
import eventRouter from './routers/eventRouter'
import multer from 'multer'
import https from 'https'
import fs from 'fs'
import { Utils } from './Utils'

mongoose.connect("mongodb+srv://*****************")
mongoose.connection.once('open', async () => {
    console.log("db ok")
})


const app = express()
app.use(express.json())
app.use(express.urlencoded({extended: true}))


app.use("/", loginRouter)
app.use("/user", userRouter)
app.use("/run", runRouter)
app.use('/uploads', express.static(path.join(__dirname, "..", "uploads")));
app.use("/room", roomRouter)
app.use("/event", eventRouter)
app.use((err: any, req: express.Request, res: express.Response, next: any) => {
    if (err instanceof multer.MulterError) {
        if (err.code === 'LIMIT_FILE_SIZE') {
            return res.json({ message: 'Max file size is 2MB.' });
        }
    }
    else if (typeof err == 'string')
        return res.json({message: err})
    else
        next(err);
});

const privateKey = fs.readFileSync(Utils.privateKeyPath())
const certificate = fs.readFileSync(Utils.certificatePath())
const credentials = { key: privateKey, cert: certificate }
const httpsServer = https.createServer(credentials, app)
const port = process.env.PORT || 4000;
httpsServer.listen(port, () => { console.log("Express server running on port " + port + " using HTTPS.") })
//app.listen(port, () => console.log("Express server running on " + port + "."))