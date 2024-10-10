import express from 'express'
import fs from 'fs'
import { DB } from '../DB'
import { Utils } from '../Utils'
import { Validation } from '../Validations'
import { JWT } from '../JWT'
import { User } from '../models/User'
import { ObjectId } from 'mongodb'


export class UserController {

    data = async (req: express.Request, res: express.Response) => {
        let id = req.query.id
        if (!id || typeof id != 'string')
            throw "Invalid request."
        
        let user: User | null = null
        if (id.length == 24 && id.indexOf("@") == -1)
            user = await DB.userById(new ObjectId(id))
        else
            user = await DB.userByMail(id)
        if (user == null)
            throw "User does not exist."

        let ret: any = {...user}
        delete ret.password
        res.json({message: "ok", data: ret})
    }

    update = async (req: express.Request, res: express.Response) => {
        let input = req.body
        let output: Partial<User> = Validation.userUpdate(input)

        let profile: any = req.file
        if (req.file) {
            Validation.profile(profile)
            let previous = req.jwt!.profile //delete previous profile picture
            if (previous != Utils.defaultProfile()) fs.unlink(Utils.uploadPath(previous), () => {})
            let uniqueName = `${Utils.randomUniqueFileName()}.${profile.mimetype.split('/')[1]}`
            let uploadPath = Utils.uploadPath(uniqueName)
            fs.writeFileSync(uploadPath, profile.buffer)
            output.profile = uniqueName
        }
        
        await DB.updateUser(req.jwt!._id, output)
        let newToken = JWT.generateJWT((await DB.userById(req.jwt!._id))!)
        res.json({message: "ok", data: newToken})
    }
}