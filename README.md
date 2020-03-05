# GIAP

## Example App

Example app is under `example` directory, use Android Studio to build and run the app.

Make sure to update `serverUrl` and `token` in `GIAP.initialize()` before running.

## Installation

**Step 1: Add this repository as a submodule**

```sh
git submodule add https://github.com/tutoruniverse/giap_android.git
```

**Step 2: Import giap_android/giap as a module**

- In Adnroid Studio, select `File` -> `New` -> `Import Module...`
- Choose `giap_android/giap`

**Step 3: Implement giap in build.gradle**

- Open `build.gradle` of your app
- Add this line

```text
...
dependencies {
    ...
    implementation project(':giap')
    ...
}
```

## Usage

### Initialize

```java
final GIAP giap = GIAP.initialize(
    "http://server-url.example",
    "your_project_token",
    MainActivity.this
);
```

### Create alias

Use this method right after user has just signed up.

```java
giap.alias(userId);
```

### Identify

Use this method right after user has just logged in.

```java
giap.identify(userId);
```

### Track event

Use a string to represent the event name and a JSONObject to contain all custom properties.

```java
// With custom props
try {
    JSONObject props = new JSONObject();
    props.put("price", 100);
    props.put("package_sku", "package_1_free");
    giap.track("Purchase", props);
} catch (JSONException e) {
    e.printStackTrace();
}

// Without custom props
giap.track("End session");
```

### Set properties for current profile

At any moment after initializing the lib, you can set custom properties for current tracking profile.

```java
try {
    JSONObject props = new JSONObject();
    props.put("full_name", "John Doe");
    giap.updateProfile(props);
} catch (JSONException e) {
    e.printStackTrace();
}
```

### Update profile properties atomically
Increase/Decrease a numeric property

```java
giap.increaseProperty("count", 1);
```
Append new elements to a list property

```java
JSONArray array = new JSONArray();
array.put("red");
array.put("blue");
giap.appendToProperty("tags", array);
```
Remove elements from a list property

```java
JSONArray array = new JSONArray();
array.put("red");
array.put("blue");
giap.removeFromProperty("tags", array);
```

### Reset

Use this method right after user has just logged out. It will generate new distinct ID for new visitor.

```java
giap.reset();
```
