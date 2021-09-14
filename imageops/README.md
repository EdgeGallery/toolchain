1. git clone toolchain repo

```
git clone https://gitee.com/edgegallery/toolchain.git
```

2. launch the imageops service

```
cd toolchain/imageops
python imageops/api/routes.py
```

3. example of check API

```
curl -X POST -H "Content-Type: application/json" -d '{"inputImageName": "ChinaUnicomAR.img"}'  http://127.0.0.1:5000/api/v1/vmimage/check
```



