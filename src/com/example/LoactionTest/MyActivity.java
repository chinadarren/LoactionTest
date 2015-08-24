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
     * ��λ����
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

        //�����ǻ�ȡ���� LocationManager ��ʵ��
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //��ȡ���п��õ�λ���ṩ��
        List<String> providerList = locationManager.getProviders(true);
        //�ж��ṩ��
        if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            proivder = locationManager.NETWORK_PROVIDER;
        } else if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            proivder = locationManager.GPS_PROVIDER;
        } else {
            //��û�п��õ�λ���ṩ��ʱ������Toast��ʾ�û�
            Toast.makeText(this, "NO loaction provider to use", Toast.LENGTH_SHORT).show();
            return;
        }
        //���� getLastKnownLocation()�������Ի�ȡ����¼��ǰλ����Ϣ��Location����
        Location location = locationManager.getLastKnownLocation(proivder);
        if (location != null) {
            showLocation(location);
        }

        //�ڲ�������д��
        //���� requestLocationUpdates()���������һ��λ�ü�����
//        locationManager.requestLocationUpdates(proivder, 1000, 0, new LocationListener() {
//            @Override
//            public void onLocationChanged(Location location) {
//                // ���µ�ǰ�豸��λ����Ϣ
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

    //���� requestLocationUpdates()���������һ��λ�ü�����
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // ���µ�ǰ�豸��λ����Ϣ
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
                    //��װ����������Ľӿ�
                    StringBuilder url = new StringBuilder();
                    url.append("http://maps.googleapis.com/maps/api/geocode/json?latlng=");
                    url.append(location.getLatitude()).append(",-");
                    url.append(location.getLongitude());
                    url.append("&sensor=false");
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(url.toString());
                    Log.d("MaActivity",url.toString());
                    //��������Ϣͷ��ָ�����ԣ���֤�������᷵����������
                    httpGet.addHeader("Accept-Language","zh-CN");
                    HttpResponse httpResponse = httpClient.execute(httpGet);
               //     if (httpResponse.getStatusLine().getStatusCode() == 200){
                        HttpEntity entity = httpResponse.getEntity();
                        String response = EntityUtils.toString(entity, "utf-8");
                        JSONObject jsonObject = new JSONObject(response);
                        //��ȡresults�ڵ��µ�λ����Ϣ
                        JSONArray resultArray = jsonObject.getJSONArray("results");
                        if (resultArray.length()>0){
                            JSONObject subObject = resultArray.getJSONObject(0);
                            //ȡ����ʽ����λ����Ϣ
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

//StringBuilderд��
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

