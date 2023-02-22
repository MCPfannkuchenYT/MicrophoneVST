# MicrophoneVST
MicrophoneVST is a simple vst audio processor. It takes the input from your microphone and applies a chain of vsts to it then routing it back into a virtual microphone.

## How does it work?
Once opened the program will be available in the system tray and run in the background.
You can configure the active input, output and vst chain from here.

## Why two different asio devices?
Complicated. I was only able to find good directsound and asio apis, first one has a much too high latency for a microphone. Meanwhile an ASIO combined with FlexASIO gives access to all audio apis with low latency anyways.
Additionally it's not possible to bridge asio devices easily, therefore I wanted to program a solution to this.

## Where's the project at right now?
uhhh.. i got vst to work... one asio too... 20 minutes?