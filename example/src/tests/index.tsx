import { applicationFileTests } from "./applicationFiles";

interface Test {
    title: string;
    handler(): Promise<boolean>;
}

export const tests: Test[] = [
    ...applicationFileTests
]
