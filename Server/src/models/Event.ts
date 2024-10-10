import { ObjectId } from 'mongodb'
import mongoose, {InferSchemaType} from 'mongoose'

const latLngSchema = new mongoose.Schema({
    latitude: {
        type: Number,
        required: true
    },
    longitude: {
        type: Number,
        required: true
    }
})
const eventSchema = new mongoose.Schema({
    name: {
        type: String,
        required: true
    },
    description: {
        type: String,
        required: true,
        default: ""
    },
    image: {
        type: String,
        required: true
    },
    time: {
        type: Number,
        required: true
    },
    distance: {
        type: Number,
        required: true
    },
    path: {
        type: [latLngSchema],
        required: false,
        default: null
    },
    tolerance: {
        type: Number,
        required: false,
        default: null
    },
    followers: {
        type: [mongoose.Schema.Types.ObjectId],
        required: true,
        default: []
    }
})

export type Event = InferSchemaType<typeof eventSchema> & {_id?: ObjectId}
export type EventRanking = {
    user: mongoose.Schema.Types.ObjectId,
    name: string,
    last: string,
    time: number,
    disqualified: boolean
}
export default mongoose.model("eventModel", eventSchema, "events")