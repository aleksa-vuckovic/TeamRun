import express from 'express'
import { DB } from '../DB'
import bcrypt from 'bcrypt'
import { JWT } from '../JWT'
import { Validation } from '../Validations'
import fs from 'fs'
import { Utils } from '../Utils'
import { User } from '../models/User'


export class LoginController {

    test = async (req: express.Request, res: express.Response) => {
        let sent = req.query.test;
        if (sent == "test")
            throw `Rejected with ${sent}`
        else return res.json({message:"ok"})
        
        //res.status(400).type("text/plain").send("Some plain text.")
    }

    login = async (req: express.Request, res: express.Response) => {
        let user = await DB.userByMail(req.body.email)
        if (user == null) throw "Email does not exist."

        if (!(await bcrypt.compare(req.body.password, user.password!)))
            throw "Incorrect password."

        let token = JWT.generateJWT(user)
        let ret: any = {...user}
        delete ret.password
        res.json({message: "ok", data: {token: token, user: ret}})
    }

    refresh = async(req: express.Request, res: express.Response) => {
        let user = await DB.userById(req.jwt!._id!)
        let newToken = JWT.generateJWT(user!)
        res.json({message: "ok", data: newToken})
    }

    register = async(req: express.Request, res: express.Response) => {
        let input = req.body
        let output: User = await Validation.registration(input)
        let profile: any = req.file
        Validation.profile(profile)
        let uniqueName = `${Utils.randomUniqueFileName()}.${profile.mimetype.split('/')[1]}`
        let uploadPath = Utils.uploadPath(uniqueName)
        fs.writeFileSync(uploadPath, profile.buffer)
        output.profile = uniqueName
        let salt = await bcrypt.genSalt()
        output.password = await bcrypt.hash(output.password, salt)
        await DB.addUser(output)
        let user = await DB.userByMail(output.email)
        let token = JWT.generateJWT(user!)
        let ret: any = {...user}
        delete ret.password
        res.json({message: "ok", data: {token: token, user: ret}})
        return
    }

}