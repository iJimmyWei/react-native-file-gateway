import { NativeModules } from "react-native";

const { FileGateway } = NativeModules;
const { readFile, writeFile, listFiles, exists, deleteFile, deleteDirectory,
    isDirectory, moveDirectory, status }: RawFileGatewayType = FileGateway;

export type Intention = "application" | "ephemeral" | "persistent";
export type Collection = "audio" | "image" | "video" | "download";

interface RawStatus {
    size: number;
    mimeType?: string;
    extension: string;
    nameWithoutExtension: string;
    lastModified: string;
    creationTime?: string; // API Level 26 and above only 
    lastAccessedTime?: string; // API Level 26 and above only
}

type RawFileGatewayType = {
    // File operations
    readFile(path: string, encoding: Encoding): Promise<string>; //to:do encoding opt
    writeFile(fileName: string, data: string, intention: Intention, collection: Collection): Promise<string>; //to:do encoding opt
    deleteFile(path: string): Promise<string>;
    status(path: string): Promise<RawStatus>;

    // Directory operations
    listFiles(path: string, recursive: boolean): Promise<string[]>;
    isDirectory(path: string): Promise<boolean>;
    moveDirectory(path: string, targetPath: string): Promise<string>;
    deleteDirectory(path: string): Promise<string>;

    // Misc operations
    exists(path: string): Promise<boolean>;
};

function writeFilesGateway(fileName: string, data: string, intention: Intention, collection?: Collection) {
    //TODO: determine what the collection should be if not specified using the mime
    if (!collection) {
        collection = "download";
    }

    return writeFile(fileName, data, intention, collection);
}

function listFilesGateway(path: string, recursive?: boolean): Promise<string[]> {
    return listFiles(path, recursive ?? false);
}

export type Encoding = "utf-8" | "base64";
function readFileGateway(path: string, encoding?: Encoding): Promise<string> {
    const defaultEncoding: Encoding = "utf-8";
    
    return readFile(path, encoding ?? defaultEncoding);
}

export const Dirs: {
    Cache: string,
    Application: string
} = FileGateway.getConstants();

export interface FileGatewayType extends RawFileGatewayType {
    listFiles: typeof listFilesGateway;
    readFile: typeof readFileGateway;
    writeFile: typeof writeFilesGateway;
}

const fileGateway: FileGatewayType = {
    readFile: readFileGateway,
    listFiles: listFilesGateway,
    writeFile: writeFilesGateway,
    exists,
    isDirectory,
    moveDirectory,
    deleteFile,
    deleteDirectory,
    status
};

export default fileGateway;
