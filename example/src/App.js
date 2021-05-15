import * as React from "react";
import { StyleSheet, View, Text } from "react-native";
export default function App() {
    const [result] = React.useState();
    return (React.createElement(View, { style: styles.container },
        React.createElement(Text, null,
            "Result: ",
            result)));
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
