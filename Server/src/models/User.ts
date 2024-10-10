import mongoose, {InferSchemaType} from "mongoose";
import {ObjectId} from 'mongodb';

let userSchema = new mongoose.Schema({
    email: {
        type: String,
        required: true
    },
    password: {
        type: String,
        required: true
    },
    name: {
        type: String,
        required: true
    },
    last: {
        type: String,
        required: true
    },
    profile: {
        type: String,
        required: true
    },
    weight: {
        type: Number,
        required: true
    }
})

export type User = InferSchemaType<typeof userSchema> & {_id?: ObjectId}
export type UserPayload = Omit<User, "password"> & {_id: ObjectId}
export default mongoose.model("userModel", userSchema, "users")