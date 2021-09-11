import { FileGateway } from "../App";
import faker from "faker";

async function testWriteReadApplicationFile(): Promise<boolean> {  
    const file = {
        name: faker.internet.userName(),
        contents: faker.lorem.paragraph()
    }
    const path = await FileGateway.writeFile(file.name, file.contents, "application");
    const contents = await FileGateway.readFile(path);
    if (contents === file.contents) {
        return true;
    }

    return false;
}

async function testWriteReadEmphemeralFile(): Promise<boolean> {  
    const file = {
        name: faker.internet.userName(),
        contents: faker.lorem.paragraph()
    }
    const path = await FileGateway.writeFile(file.name, file.contents, "ephemeral");
    const contents = await FileGateway.readFile(path);
    if (contents === file.contents) {
        return true;
    }

    return false;
}

export const applicationFileTests = [
    {
        title: "should write/read a file to application storage",
        handler: testWriteReadApplicationFile
    },
    {
        title: "should write/read a file to ephemeral storage",
        handler: testWriteReadEmphemeralFile
    }
]