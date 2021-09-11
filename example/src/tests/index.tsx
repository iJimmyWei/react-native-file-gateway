import { applicationFileTests } from "./writeRead";
import listFilesTests from "./listFiles";
import deleteFileTests from "./deleteFiles";
import statusTests from "./status";

interface Test {
    title: string;
    handler(): Promise<boolean>;
}

export const tests: Test[] = [
    ...applicationFileTests,
    ...listFilesTests,
    ...deleteFileTests,
    ...statusTests
]
