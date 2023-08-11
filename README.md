<p align="center">
  <br />
  <br />
  <a href="https://mouwrice.github.io/proguard-core-visualizer/download.html">
    <img
      src="https://github.com/Mouwrice/proguard-core-visualizer/assets/56763273/e3a08c7f-0eb5-41ec-9ef9-09b11d907bc7"
      alt="ProGuardCORE Visualizer" width="400">
  </a>
</p>


<h4 align="center">A desktop application to visualize evaluations from <a href="https://github.com/Guardsquare/proguard-core"> ProGuardCORE</a></h4>

> [!IMPORTANT]
> We have moved. You can find the new location of this project at the following address: https://github.com/Guardsquare/proguard-core-visualizer

## ❓ Getting Help
If you have **usage or general questions**, do not hesitate to [make a discussion](https://github.com/Mouwrice/proguard-core-visualizer/discussions/new/choose).

Please create an [Issue](https://github.com/Mouwrice/proguard-core-visualizer/issues/new) to report actual **bugs 🐛, crashes**, etc.

## 💾 Installation

The latest release is available for download on the following page:

📩 https://mouwrice.github.io/proguard-core-visualizer/download.html

### Supported platforms
#### Windows
#### MacOS
Works on both Intel and Apple silicon machines.
#### Linux
- `.deb` Debian package format supported
- `.rpm`, Snap, FlatPak (Not yet supported: https://conveyor.hydraulic.dev/10.1/outputs/#other-linux-package-formats)
- Generic tarball


## ✨ Features

If you have never heard of ProGuard then this tool might not be for you 😃

If you want to see what evaluating instructions looks like, we got your back.
The visualizer provides a visual representation of the evaluations made by the [partial evaluator](https://guardsquare.github.io/proguard-core/partialevaluator.html) from ProGuard Core.
It does this through the [JsonPrinter API](https://github.com/Guardsquare/proguard-core/blob/master/base/src/main/java/proguard/evaluation/util/jsonprinter/JsonPrinter.java)
provided by ProGuard Core.

In general, we can help you:
* Step through the partial evaluation step-by-step while keeping track of:
the stack, the variables, the to-be-evaluated-branch start offsets, whether an instruction is generalized and whether an instruction is skipped.
* View the final result from the evaluator for each instruction, see the stack, the variables, origin instructions and target instructions.
* Navigate freely through the evaluations by simply clicking the instructions to view desired evaluation.
* Navigate a large file through our search bar, go where you need to be quickly.
* Save the JSON debug file of an evaluation.

JSON and JBC files can be edited using an integrated editor through scratch files. 
Simply create a scratch file and start editing it.

Lastly for AAR, APK, CLASS, DEX, JAR, ZIP and JBC files, we allow evaluation of using a value factory of choice.
This way you can make sure you choose the value factory that's just write for you. 

The repository contains some samples of files that can be opened in the [examples](examples) directory.

NOTE: we do not support the visualization of the subroutine evaluations caused by JSR.
Whenever a JSR instruction is evaluated, we just skip over the subroutine evaluation.

## 🤝 Contributing

Contributions, issues and feature requests are welcome.
Feel free to check the [issues](https://github.com/Mouwrice/proguard-core-visualizer/issues) page and the [contributing
guide](CONTRIBUTING.md) if you would like to contribute.

## 📝 License

Copyright (c) 2023 [Guardsquare NV](https://www.guardsquare.com/).
ProGuardCORE Visualizer is released under the [Apache 2 license](LICENSE).
