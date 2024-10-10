import { ObjectId } from 'mongodb'
import mongoose, {InferSchemaType} from 'mongoose'

const roomSchema = new mongoose.Schema({
    members: {
        type: [mongoose.Schema.Types.ObjectId],
        required: true,
        default: []
    },
    ready: {
        type: [mongoose.Schema.Types.ObjectId],
        required: true,
        default: []
    },
    start: {
        type: Number,
        required: false,
        default: null
    }
})

export type Room = InferSchemaType<typeof roomSchema> & {_id?: ObjectId}
export default mongoose.model("roomModel", roomSchema, "rooms")