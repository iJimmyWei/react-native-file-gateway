import { FileGateway } from "../App";
import faker from "faker";

async function testStatus(): Promise<boolean> {
    const file = {
        name: faker.internet.userName(),
        contents: faker.lorem.paragraph()
    }

    const path = await FileGateway.writeFile(file.name + ".png", file.contents, "application");

    const status = await FileGateway.status(path);
    if (
        status.extension === "png" &&
        status.nameWithoutExtension === file.name &&
        status.mime === "image/png" &&
        status.size > 0 &&
        status.creationTime !== undefined &&
        status.lastModified !== undefined
    ) {
        return true;
    }

    return false;
}

export default [
    {
        title: "should show status of a file",
        handler: testStatus
    }
]