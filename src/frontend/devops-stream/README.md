Git Ci
===

``` bash
# create `Dll bundle` with webpack DllPlugin
npm run dll

# generate selfsigned and move to src/conf
sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout ./selfsigned.key -out selfsigned.crt

# dev
npm run dev

# build
npm run build

# build with analyzer
npm run build:analyzer

```
