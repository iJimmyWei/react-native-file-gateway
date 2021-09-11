import * as React from "react";
import { useState } from "react";

import { StyleSheet, View, Button } from "react-native";

import FileGateway from "../../src";
export { FileGateway };

import { tests } from "./tests";

function TestCaseButton({testCaseName, testNumber, onPress}: {testCaseName: string, testNumber: number, onPress: () => Promise<boolean>}) {
    const [status, setStatus] = useState<"ready" | "failed" | "passed">("ready");

    const hasPassed = status === "passed";
    const hasFailed = status === "failed";
    
    async function runTest() {
        const passed = await onPress();
        if (passed) {
            setStatus("passed");
        } else {
            setStatus("failed");
        }
    }
    
    return (
        <Button
            title={`test case ${testNumber + 1} - ${testCaseName}`}
            onPress={runTest}
            color={hasPassed
                ? "green"
                : hasFailed
                    ? "red"
                    : "orange"
                }
        >
        </Button>
    )
}

export default function App() {
    return (
        <View style={styles.container}>
            {tests.map((test, index) => <TestCaseButton
                key={index}
                testCaseName={test.title}
                testNumber={index}
                onPress={test.handler}
            />)}
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
    },
    box: {
        width: 60,
        height: 60,
        marginVertical: 20,
    },
});
