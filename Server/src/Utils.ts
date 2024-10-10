import path from "path";
import express from "express";

export class Utils {

    static defaultProfile(): string {
        return "default.png"
    }
    static defaultEventImage(): string {
        return "default2.png"
    }
    static uploadDir(): string {
        return path.join(__dirname, "..", "uploads")
    }
    static privateKeyPath(): string {
        return path.join(__dirname, "..", "private.key")
    }
    static certificatePath(): string {
        return path.join(__dirname, "..", "certificate.crt")
    }
    static uploadPath(filename: string) {
        return path.join(this.uploadDir(), filename)
    }

    static randomUniqueFileName(): string {
        return `${Date.now()}`
    }
    static parseBoolean(input: any): boolean | null {
        if (input === false || input === "false") return false
        else if (input) return true
        else return null
    }

    static asyncHandler(fn: express.RequestHandler): express.RequestHandler {
        return (req: express.Request, res: express.Response, next: express.NextFunction) => Promise.resolve(fn(req, res, next)).catch(next)
    }
}