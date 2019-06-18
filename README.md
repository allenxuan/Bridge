# Bridge
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/index.html)
[![License](https://img.shields.io/badge/Version-0.0.2-blue.svg)](https://jcenter.bintray.com/com/github/allenxuan/)
[![License](https://img.shields.io/badge/License-Apache%202.0-red.svg)](http://www.apache.org/licenses/LICENSE-2.0)

#### Bridge is a lightweight implementation-manage framework for Android, powering cross-module invocation.

### Dependencies:
```groovy
//Kotlin
dependencies {
    compileOnly 'com.github.allenxuan:bridge-annotation:0.0.2'
    kapt 'com.github.allenxuan:bridge-compiler:0.0.2'
    implementation 'com.github.allenxuan:bridge-core:0.0.2'
}

//Java
dependencies {
    compileOnly 'com.github.allenxuan:bridge-annotation:0.0.2'
    annotationProcessor 'com.github.allenxuan:bridge-compiler:0.0.2'
    implementation 'com.github.allenxuan:bridge-core:0.0.2'
}
```

### Recommended Project Structure Example
![recommended_project_structure](/art/recommend_project_structure.png)

The demo app project just adopts this structure.

### Usage Examples
Assume you've applied the recommended project structure.
In module-api, define an interface called IImageLoader which exposes the image-load ability
```java
public interface IImageLoader {
    void load(ImageView imageView);
}
```
Implement IImageLoader in module-imageloader with annotation InterfaceTarget which specifies interface target as IImageLoader.class
```java
@InterfaceTarget(Interface = IImageLoader.class)
public class ImageLoader implements IImageLoader {
    @Override
    public void load(ImageView imageView) {
        Context context = imageView.getContext();
        if(context != null) {
            Toast.makeText(context, "load image", Toast.LENGTH_SHORT).show();
        }
        imageView.setImageResource(android.R.color.black);
    }
}
```

Clearly, module-login does not have gradle dependency
on module-imageloader, but we can still access the implementation of IImageLoader in module-login
```java
public class LoginActivity extends AppCompatActivity {
    ...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Bridge.getImpl(IImageLoader.class).load(findViewById(R.id.image_view));
    }
    ...
}
```

We can remove a implementation singleton in Bridge by
```java
Bridge.removeImpl(IImageLoader.class);
```

# License
```
Copyright 2019 Xuanyi Huang

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
