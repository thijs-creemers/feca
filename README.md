# FE coding assignment

###Prerequisites
```bash
brew install clojure yarn  # installs the clojure and yarn packages
yarn install               # installs all npm dependencies
```
## Install environment

```bash
git clone git@github.com:thijs-creemers/feca.git

cd feca
yarn clean
```
This installs a clean set of nodenpm modules and compiles the css.

## Compile and run the dev server
```bash
yarn dev
```
Wait a few moments for the compilation to complete and go the your browser and open 
the app with the url: 'http://127.0.0.1:8101'. 

## Create a optimized runtime version of the app
```bash
yarn release
```
