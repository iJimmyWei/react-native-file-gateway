import { NativeModules } from "react-native";

const { FileGateway } = NativeModules;
const { readFile, writeFile, listFiles, exists, deleteFile, isDirectory, moveDirectory }: RawFileGatewayType = FileGateway;

// export type DirectoryType = "Application" | "Cache" | "External";

type RawFileGatewayType = {
    // File operations
    readFile(path: string, encoding: Encoding): Promise<string>; //to:do encoding opt
    writeFile(fileName: string, data: string, intention: string): Promise<string>; //to:do encoding opt
    deleteFile(path: string): Promise<boolean>;


    // Directory operations
    listFiles(path: string, recursive: boolean): Promise<string[]>;
    isDirectory(path: string): Promise<boolean>;
    moveDirectory(path: string, targetPath: string): Promise<string>;

    // Misc operations
    exists(path: string): Promise<boolean>;
};

export function listFilesGateway(path: string, recursive?: boolean): Promise<string[]> {
    return listFiles(path, recursive ?? false);
}

export type Encoding = "utf-8" | "base64";
export function readFileGateway(path: string, encoding?: Encoding): Promise<string> {
    const defaultEncoding: Encoding = "utf-8";
    
    return readFile(path, encoding ?? defaultEncoding);
}

export const Dirs: {
    Cache: string,
    Application: string
} = FileGateway.getConstants();

export interface FileGatewayType extends RawFileGatewayType {
    listFiles(path: string, recursive?: boolean): Promise<string[]>;
    readFile(path: string, encoding?: Encoding): Promise<string>;
}

const fileGateway: FileGatewayType = {
    readFile: readFileGateway,
    listFiles: listFilesGateway,
    writeFile,
    exists,
    isDirectory,
    moveDirectory,
    deleteFile
};

export default fileGateway;
