import { FileGateway } from "../App";
import faker from "faker";

async function testListFiles(): Promise<boolean> {
    const file = {
        name: faker.internet.userName(),
        contents: faker.lorem.paragraph()
    }

    const secondFileName = file.name + "1";
    await FileGateway.writeFile(file.name, file.contents, "application");
    await FileGateway.writeFile(secondFileName, file.contents, "application");

    const contents = await FileGateway.listFiles(FileGateway.constants.ApplicationDirectory);
    if (
        contents.includes(file.name) &&
        contents.includes(secondFileName)
    ) {
        return true;
    }

    return false;
}

export default [
    {
        title: "should list files in application storage",
        handler: testListFiles
    }
]