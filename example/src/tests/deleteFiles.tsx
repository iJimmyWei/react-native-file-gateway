import { FileGateway } from "../App";
import faker from "faker";

async function testDeleteFile(): Promise<boolean> {  
    const file = {
        name: faker.internet.userName(),
        contents: faker.lorem.paragraph()
    }
    const path = await FileGateway.writeFile(file.name, file.contents, "application");
    const deletedPath = await FileGateway.deleteFile(path);

    if (path !== deletedPath) {
        return false;
    }

    const exists = await FileGateway.exists(deletedPath);
    if (exists) {
        return false;
    }

    return true;
}

async function testDeleteNonExistantFile(): Promise<boolean> { 
    try {
        await FileGateway.deleteFile("aaafakefile.png");

        return false;        
    } catch (e) {
        const exception = e as { message: string };
        if (exception.message === "The file does not exist") {
            return true;
        }

        return false;
    }
}

async function testDeleteDirectory(): Promise<boolean> { 
    try {
        await FileGateway.deleteFile(FileGateway.constants.ApplicationDirectory);

        return false;        
    } catch (e) {
        const exception = e as { message: string };
        if (exception.message === "The file is a directory") {
            return true;
        }

        return false;
    }
}

export default [
    {
        title: "given a file exists, should delete a file",
        handler: testDeleteFile
    },
    {
        title: "given a does file exist, should show error",
        handler: testDeleteNonExistantFile
    },
    {
        title: "given path is a directory, should show error",
        handler: testDeleteDirectory
    }
]