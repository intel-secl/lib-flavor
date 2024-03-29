DISCONTINUATION OF PROJECT

This project will no longer be maintained by Intel.

Intel has ceased development and contributions including, but not limited to, maintenance, bug fixes, new releases, or updates, to this project.  

Intel no longer accepts patches to this project.

If you have an ongoing need to use this project, are interested in independently developing it, or would like to maintain patches for the open source software community, please create your own fork of this project.  
# Intel<sup>®</sup> Security Libraries for Data Center  - Flavor Library
#### This library is responsible for extracting the whitelist measurements from the host, which is being used a good known host for verifying against other target systems with similar configuration. These measurements correspond to specific system components and are used as the basis of comparison to generate trust attestations. There are multiple types of flavor including PLATFORM, OS, ASSET_TAG and HOST_UNIQUE.

## Key features
- Provides interface to create good known configuration in the form of Json from the information retrieved from host
- This configuration is type casted in PLATFORM, OS, ASSET_TAG and HOST_UNIQUE

## System Requirements
- RHEL 7.5/7.6
- Epel 7 Repo
- Proxy settings if applicable

## Software requirements
- git
- maven (v3.3.1)
- ant (v1.9.10 or more)

# Step By Step Build Instructions
## Install required shell commands
Please make sure that you have the right `http proxy` settings if you are behind a proxy
```shell
export HTTP_PROXY=http://<proxy>:<port>
export HTTPS_PROXY=https://<proxy>:<port>
```
### Install tools from `yum`
```shell
$ sudo yum install -y wget git zip unzip ant gcc patch gcc-c++ trousers-devel openssl-devel makeself
```

## Direct dependencies
Following repositories needs to be build before building this repository,

| Name                       | Repo URL                                                 |
| -------------------------- | -------------------------------------------------------- |
| common-java                | https://github.com/intel-secl/common-java                |
| lib-common                 | https://github.com/intel-secl/lib-common                 |
| lib-host-connector         | https://github.com/intel-secl/lib-host-connector         |

## Build Flavor Library

- Git clone the `Flavor Library`
- Run scripts to build the `Flavor Library`

```shell
$ git clone https://github.com/intel-secl/lib-flavor.git
$ cd lib-flavor
$ ant
```

# Links
 - Use [Automated Build Steps](https://01.org/intel-secl/documentation/build-installation-scripts) to build all repositories in one go, this will also provide provision to install prerequisites and would handle order and version of dependent repositories.

***Note:** Automated script would install a specific version of the build tools, which might be different than the one you are currently using*
 - [Product Documentation](https://01.org/intel-secl/documentation/intel%C2%AE-secl-dc-product-guide)
