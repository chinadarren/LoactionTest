package com.example.LoactionTest;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;


public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     * 定位服务
     */
    private Button bt;
    private TextView tv;
    private LocationManager locationManager;
    private String proivder;
    public static final int SHOW_LOCATION = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        bt = (Button) findViewById(R.id.button);
        tv = (TextView) findViewById(R.id.text_view);

        //首先是获取到了 LocationManager 的实例
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //获取所有可用的位置提供器
        List<String> providerList = locationManager.getProviders(true);
        //判断提供器
        if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            proivder = locationManager.NETWORK_PROVIDER;
        } else if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            proivder = locationManager.GPS_PROVIDER;
        } else {
            //当没有可用的位置提供器时，弹出Toast提示用户
            Toast.makeText(this, "NO loaction provider to use", Toast.LENGTH_SHORT).show();
            return;
        }
        //调用 getLastKnownLocation()方法可以获取到记录当前位置信息的Location对象
        Location location = locationManager.getLastKnownLocation(proivder);
        if (location != null) {
            showLocation(location);
        }

        //内部匿名类写法
        //调用 requestLocationUpdates()方法来添加一个位置监听器
//        locationManager.requestLocationUpdates(proivder, 1000, 0, new LocationListener() {
//            @Override
//            public void onLocationChanged(Location location) {
//                // 更新当前设备的位置信息
//                showLocation(location);
//            }
//            @Override
//            public void onStatusChanged(String provider, int status, Bundle extras) {
//
//            }
//            @Override
//            public void onProviderEnabled(String provider) {
//
//            }
//            @Override
//            public void onProviderDisabled(String provider) {
//
//            }
//        }
//        );
        locationManager.requestLocationUpdates(proivder, 1000, 0, locationListener);

    }

    //调用 requestLocationUpdates()方法来添加一个位置监听器
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // 更新当前设备的位置信息
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    private void showLocation(final Location location) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    //组装反响地理编码的接口
                    StringBuilder url = new StringBuilder();
                    url.append("http://maps.googleapis.com/maps/api/geocode/json?latlng=");
                    url.append(location.getLatitude()).append(",-");
                    url.append(location.getLongitude());
                    url.append("&sensor=false");
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(url.toString());
                    Log.d("MaActivity",url.toString());
                    //在请求消息头重指定语言，保证服务器会返回中文数据
                    httpGet.addHeader("Accept-Language","zh-CN");
                    HttpResponse httpResponse = httpClient.execute(httpGet);
               //     if (httpResponse.getStatusLine().getStatusCode() == 200){
                        HttpEntity entity = httpResponse.getEntity();
                        String response = EntityUtils.toString(entity, "utf-8");
                        JSONObject jsonObject = new JSONObject(response);
                        //获取results节点下的位置信息
                        JSONArray resultArray = jsonObject.getJSONArray("results");
                        if (resultArray.length()>0){
                            JSONObject subObject = resultArray.getJSONObject(0);
                            //取出格式化的位置信息
                            String address = subObject.getString("formatted_address");
                            Message message = new Message();
                            message.what = SHOW_LOCATION;
                            message.obj = address;
                            handler.sendMessage(message);
                        }
                  //  }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

//        String sb = "Latitude is: " + location.getLatitude() + "\n" + "Longitude is: " + location.getLongitude();
//        tv.setText(sb);

//StringBuilder写法
//        StringBuilder sb = new StringBuilder();
//        sb.append("(");
//        sb.append(location.getLatitude());
//        sb.append(",");
//        sb.append(location.getLongitude());
//        sb.append(")");
//        sb.setText(sb);
    }

    private Handler handler = new Handler(){
        public void handledMessage(Message msg){
            switch (msg.what){
                case SHOW_LOCATION:
                    String currentPosition = (String) msg.obj;
                    Toast.makeText(getApplicationContext(),currentPosition,Toast.LENGTH_SHORT).show();
                    tv.setText(currentPosition);
                    break;
                default:
            }
        }
    };
}

