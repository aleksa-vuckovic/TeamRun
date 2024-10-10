import { ObjectId } from 'mongodb'
import mongoose, {InferSchemaType} from 'mongoose'

const pathPointSchema = new mongoose.Schema({
    latitude: {
        type: Number,
        default: 0,
        required: true
    },
    longitude: {
        type: Number,
        default: 0,
        required: true
    },
    altitude: {
        type: Number,
        default: 0,
        required: true
    },
    time: {
        type: Number,
        default: 0,
        required: true
    },
    end: {
        type: Boolean,
        default: false,
        required: true
    },
    speed: {
        type: Number,
        default: 0,
        required: true
    },
    distance: {
        type: Number,
        default: 0,
        required: true
    },
    kcal: {
        type: Number,
        default: 0,
        required: true
    },
}, {
    _id: false
})

const runSchema = new mongoose.Schema({
    id: {
        type: Number,
        required: true
    },
    user: {
        type: mongoose.Schema.Types.ObjectId,
        required: true
    },
    event: {
        type: mongoose.Schema.Types.ObjectId,
        required: false,
        default: null
    },
    room: {
        type: mongoose.Schema.Types.ObjectId,
        required: false,
        default: null
    },
    start: {
        type: Number,
        required: false,
        default: null
    },
    running: {
        type: Number,
        required: true,
        default: 0
    },
    end: {
        type: Number,
        required: false,
        default: null
    },
    paused: {
        type: Boolean,
        required: true,
        default: false
    },
    cur: {
        type: Number,
        required: false,
        default: null
    },
    penalty: {
        type: Number,
        required: false,
        default: null
    },
    location: {
        type: pathPointSchema,
        required: true,
        default: () => ({})
    },
    path: {
        type: [pathPointSchema],
        default: []
    }
})

export type PathPoint = InferSchemaType<typeof  pathPointSchema>
export type Run = Omit<InferSchemaType<typeof runSchema> & {_id?: ObjectId}, "path"> & {path: PathPoint[]}

export type RunInner = Omit<Run, "path" | "location">
export type RunProjection = {run:  RunInner} & {"path": PathPoint[], "location": PathPoint}
export default mongoose.model("runModel", runSchema, "runs")