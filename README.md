# React Native File Gateway
[![npm](https://img.shields.io/github/workflow/status/iJImmyWei/react-native-file-gateway/CI/main)](https://www.npmjs.com/package/react-native-file-gateway) [![npm](https://img.shields.io/npm/v/react-native-file-gateway)](https://www.npmjs.com/package/react-native-file-gateway) [![npm](https://img.shields.io/npm/dm/react-native-file-gateway)](https://www.npmjs.com/package/react-native-file-gateway) 

A React Native library making file access easier for developers as first class citizens, without the tears.

:warning: NOTE: This library is in early development, focusing primarily on Android. :warning:

## About
This library is focused around making the developer's life simpler when it comes to interacting with the native file system for React Native. This library has 3 main goals:
* Being **simple**, and easy to understand
	* This may mean some things are abstracted/opinionated (see `writeFile()` as one example of this)
* Being **performant**
	* Handling large/many files is a must. Any file interopability is always handled at the native level
* Being **well tested**
	*	Where tests are needed, they will be there. Native or at the JS level, a test will accompany said code


## Supported platforms
* Android (In development)
## Upcoming platforms
* iOS
* Windows
* MacOS

## Roadmap
* File system interoperability (In progress)
* Downloading/uploading files
* Cryptography
* Unit/integration/E2E testing (TO:DO)

## Installation

```sh
npm install react-native-file-gateway
```

## Usage

```ts

import FileGateway from  "react-native-file-gateway";

// Scenario 1: Writing an MP4 file to the secure application store.
// This file will be deleted on uninstall.
const path = await FileGateway.writeFile(
	"bigBuckBunny.mp4", // Name of the file
	"010101", // Data to be written as a string (UTF-8 encoding is default)
	"application" // Intention of the file's lifetime. Can be application, ephemeral, or persistent (see more in documentation below)
)

// Scenario 2: Writing an image file to the cache.
// This file will be deleted on uninstall.
const path = await FileGateway.writeFile(
	"bigBuckBunny.mp4",
	"010101",
	"ephemeral" // Ephemeral will store this file into the system's cache
)

```

### Functions
`writeFile(fileName: string, data: string, intention: Intention, collection?: Collection): Promise<string>` - Writes to a file and **returns it's path** as a `Promise<string>`
-  Intention `("application"  |  "ephemeral"  |  "persistent")`
     - The **intention** is to indicate how the file will be stored.
	     -  `application` intention will allocate the file into the application's own storage, but other applications won't be able to access it. The file will be lost on uninstall.
	     - `ephemeral` intention will allocate the file into the cache storage. The file will be lost on uninstall
	     - `persistent` intention will allocate the file into external storage. The file will persist on uninstall. Useful for media content.
-  Collection `("audio"  |  "image"  |  "video"  | "download")`
     - The **collection** is to indicate where the file will be stored (ONLY APPLICABLE FOR `persistent` intentions). Fallsback to `download` if not specified. 
---
  
`readFile(path: string, encoding: Encoding): Promise<string>` - Reads a given file, given it's path and **returns the data** as a `Promise<string>`
-  Encoding `("utf-8")`
	* Sets the encoding when reading the file
---
  
`deleteFile(path: string): Promise<string>` - Deletes a file, given it's path and **returns the path** if successful as a `Promise<string>`

---
`status(path: string): Promise<RawStatus>` - Returns back the `Promise<RawStatus>` of a file, given it's `path`
  * `RawStatus` includes the following
	  * `size` (in bytes)
	  * `mimeType` (e.g `application/javascript`)
	  * `extension` (e.g `mp3`)
	  * `nameWithoutExtension` (e.g `bigBuckBunny`)
	  * `lastModified` (e.g `2021-05-19T21:10:48.197Z`- ISO UTC format)
	  * `creationTime` (e.g `2021-05-19T21:10:48.197Z`- ISO UTC format)
	  * `lastAccessedTime` (e.g `2021-05-19T21:10:48.197Z`- ISO UTC format) 
---
`listFiles(path: string, recursive?: boolean): Promise<string[]>` - List all the files, given it's path and **returns all the file names** as a `Promise<string[]>`
  * Specifying `recursive` as `true` will recursively search through every directory for the given path

---
`exists(path: string): Promise<boolean>` - Checks if a file or directory exists. **Returns `true` or `false`** if a file exists, or doesn't, respectively as a `Promise<boolean>`

---
`isDirectory(path: string): Promise<boolean>` - Checks if the given path is a directory exists. **Returns `true` or `false`** if it's a directory, or not, respectively as a `Promise<boolean>`

---
`moveDirectory(path: string, targetPath: string): Promise<string>` - Moves a directory from the `path` to the `targetPath` (the destination). Any files at the destination will be overridden by default. **Returns back the `targetPath`** as a `Promise<string>`

---
  
`deleteDirectory(path: string): Promise<string>` - Deletes a directory, given it's path and **returns the path** if successful as a `Promise<string>`


## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT