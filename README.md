### Npm Install

```shell
$ npm install --save git+ssh://git@github.com:cyberjon/react-native-cq-share.git
```

### Automatically Link

```shell
$ react-native link react-native-cq-share
```


#### Use
```
import React, { Component } from 'react';
import {
    Platform,
    StyleSheet,
    Text,
    View,
    TouchableOpacity
} from 'react-native';

import { share } from 'react-native-cq-share'

export default class App extends Component<Props> {
render() {
    return (
        <View style={styles.container}>
            <TouchableOpacity style={{height:60,width:100,backgroundColor:'red'}}
                              onPress={()=>share(options,callbake)}>
            </TouchableOpacity>
        </View>
        );
    }
}
```
#### Parameters

### iOS
```
share:
options:
        title(String)
        url(String)
        remoteImages(Array)
        localImages(Array)
callback:  return true or false
```

### Android
```
share:
options: 
        title(String)
        remoteImages(Array)
        localImages(Array)
        description(String)    
```
