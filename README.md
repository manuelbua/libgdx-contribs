[![Flattr this git repo](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=manuelbua&url=https://github.com/manuelbua/libgdx-contribs&title=libgdx-contribs&language=&tags=github&category=software) 

libgdx-contribs
===============
[homepage](http://manuelbua.github.io/libgdx-contribs/)

My contributions and utilities for the awesome libgdx game development framework.
Project files and instructions are for Eclipse 3.7.2 (Indigo) or higher, but others may work as well with some adaptation.

All available projects in the repository will be added to the following list with a brief description:

__utils__ *Common utilities used across the contributions.*
[__post-processing__](https://github.com/manuelbua/libgdx-contribs/tree/master/postprocessing) *A lightweight, GLES2-only library to ease development and inclusion of post-processing effects in libgdx applications and games.*


***


### Cloning and importing the projects in Eclipse ###

Start by cloning the *libgdx-contribs* repository in your preferred directory:

    https://github.com/manuelbua/libgdx-contribs.git

Then import libgdx-contribs-**utils** and choose to import the project you want, for example libgdx-contribs-**postprocessing**.

In Eclipse, *File->Import* and point it to the *utils* folder, accept the defaults or change to your likings and import the project, then do the same with the project you choose to use.
At this point you should have both projects built, if *Build automatically* has been setup in your workspace, else build them manually.

### Demo applications ###

I plan to add demos to the projects, so look for a **demo** folter inside the project folder you have choosen, for example the *postprocessing* project have one, so just *File->Import* in Eclipse and point it to the `postprocessing/demo` folder to import it into your workspace.
