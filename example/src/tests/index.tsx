import { applicationFileTests } from "./writeRead";
import listFilesTests from "./listFiles";

interface Test {
    title: string;
    handler(): Promise<boolean>;
}

export const tests: Test[] = [
    ...applicationFileTests,
    ...listFilesTests
]
