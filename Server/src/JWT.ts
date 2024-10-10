import jwt from 'jsonwebtoken'
import express from 'express'
import { User, UserPayload } from './models/User'
import { ObjectId } from 'mongodb'

declare global {
    namespace Express {
        interface Request {
            jwt?: UserPayload
        }
    }
}

var secret: string = "psychic life of power"

export class JWT {
    static generateJWT(user: User): string {
        let payload: UserPayload = {
            _id: user._id!,
            email: user.email,
            name: user.name,
            last: user.last,
            profile: user.profile,
            weight: user.weight
        }
        return jwt.sign(payload, secret, {expiresIn: "3d"})
    }

    static authenticateJWT(req: express.Request, res: express.Response, next: express.NextFunction) {
        const auth = req.headers["authorization"]
        if (!auth) res.status(401).type("text/plain").send("Failed to authenticate.")
        else {
            let token = auth.split(" ")[1]
            jwt.verify(token, secret, (err, decoded: any) => {
                if (err) res.status(401).type("text/plain").send(err.message)
                else {
                    decoded._id = new ObjectId(decoded._id)
                    req.jwt = decoded as UserPayload
                    next()
                }
            })
        }
    }
}







