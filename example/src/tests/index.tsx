import { applicationFileTests } from "./writeRead";

interface Test {
    title: string;
    handler(): Promise<boolean>;
}

export const tests: Test[] = [
    ...applicationFileTests
]
