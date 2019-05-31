using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;
using UnityEngine.UI;

public class ARcameraLocation : MonoBehaviour
{
    // 실제 위도, 경도를 유니티 상의 위치로 바꾸기 위한 상수값
    private const float hRatio = 4.709577754891864f;
    private const float wRatio = 5.9751588677065286f;
    private const float hBias = 19976.100809096872f;
    private const float wBias = 8949.552837667987f;
    private const float aRatio = 0.5489942435806222f;

    private float _latitude;
    private float _longitude;
    private float _altitude;
    private float _horizontalAccuracy;
    private float _verticalAccuracy;
    private float _x;  // longitude에 의해 바뀜
    private float _y;  // altitude에 의해 바뀜
    private float _z;  // latitude에 의해 바뀜

    [SerializeField]
    private Text _text;
    [SerializeField]
    private Text _menuText;
    [SerializeField]
    private GameObject _map;

    // Start is called before the first frame update
    void Start()
    {
        Input.location.Start();
        Input.gyro.enabled = true;
        Input.gyro.updateInterval = 0.01f;
        _map.SetActive(false);
    }

    // Update is called once per frame
    void Update()
    {
        Invoke("Move", 0.2f);
        Invoke("GyroRotate", 0.2f);
    }

    //private IEnumerator Move()
    //{
    //    if(!Input.location.isEnabledByUser)
    //    {
    //        Debug.Log("not enabled GPS");
    //        text.text = "not enabled GPS";
    //        yield break;
    //    }

    //    Input.location.Start();

    //    int maxWait = 20;
    //    while(Input.location.status == LocationServiceStatus.Initializing && maxWait > 0)
    //    {
    //        yield return new WaitForSeconds(1);
    //        maxWait--;
    //    }

    //    if(maxWait <= 0)
    //    {
    //        Debug.Log("Timed out");
    //        text.text = "Timed out";
    //        yield break;
    //    }

    //    if(Input.location.status == LocationServiceStatus.Failed)
    //    {
    //        Debug.Log("Unable to determin device location");
    //        text.text = "Unable to determin device location";
    //        yield break;
    //    }

    //    if(num == 0)
    //    {
    //        _prevLatitude = Input.location.lastData.latitude;
    //        _prevLongitude = Input.location.lastData.longitude;
    //        num = 1;
    //    }

    //    num++;

    //    _curLatitude = Input.location.lastData.latitude;
    //    _curLongitude = Input.location.lastData.longitude;

    //    _moveX = (float)GetX(_prevLatitude, _prevLongitude, _curLatitude, _curLongitude);
    //    _moveZ = (float)GetX(_prevLatitude, _prevLongitude, _curLatitude, _curLongitude);

    //    _curX += _moveX;
    //    _curZ += _moveZ;
    //    transform.position = new Vector3(_curX, 0, _curZ);

    //    text.text = _curLatitude + "    " + _curLongitude + "\n" + _curX.ToString() + "        " + _curZ.ToString() + "\n" + num;

    //    _prevLatitude = _curLatitude;
    //    _prevLongitude = _curLongitude;

    //    Input.location.Stop();
    //    yield return new WaitForSeconds(0.2f);

    //    //yield break;
    //}

    private void Move()
    {
        //_latitude = Input.location.lastData.latitude;
        //_longitude = Input.location.lastData.longitude;

        //_x = GetX(_longitude);
        //_y = 3.09657163053723f;
        //_z = GetZ(_latitude);
        _x = 669;
        _y = 3.09657163053723f;
        _z = 507.3f;

        _text.text = _latitude.ToString() + "     " + _longitude.ToString() + "     " + "\n" + _x.ToString() + "     " + _z.ToString() + "     " + "\n" + _horizontalAccuracy + "     " + _verticalAccuracy.ToString();

        transform.position = new Vector3(_x, _y‬‬, _z);
    }

    //private void GetLocation()
    //{
    //    if(Input.location.status == LocationServiceStatus.Running && checkAgain)
    //    {
    //        Input.location.Stop();
    //        Input.location.Start();

    //        _verticalAccuracy = Input.location.lastData.verticalAccuracy;
    //        _horizontalAccuracy = Input.location.lastData.horizontalAccuracy;
    //    }

    //    if(_horizontalAccuracy > 18 && _verticalAccuracy > 18)
    //    {
    //        _latitude = Input.location.lastData.latitude;
    //        _longitude = Input.location.lastData.longitude;
    //        _altitude = Input.location.lastData.altitude;
    //    }
    //    else
    //        GetLocation(true);
    //}

    private float GetX(float longitude)
    {
        return _longitude * 1000000.0f % 100000.0f / wRatio - wBias;
    }

    private float GetY(float altitude)
    {
        return (altitude - 20) / aRatio;
    }

    private float GetZ(float latitude)
    {
        return _latitude * 1000000 % 100000 / hRatio - hBias;
    }

    private void GyroRotate()
    {
        Quaternion transquat = Quaternion.identity;
        transquat.w = Input.gyro.attitude.w;
        transquat.x = Input.gyro.attitude.x;
        transquat.y = Input.gyro.attitude.y;
        transquat.z = Input.gyro.attitude.z;

        transform.rotation = Quaternion.Euler(90, 0, 180) * transquat;
    }

/*    // lat1, lon1 = 기존 좌표, lat2, lon2 = 새로 이동한 좌표
    private static double GetX(double lat1, double lon1, double lat2, double lon2)
    {
        lat1 = lat2;
        double theta = lon1 - lon2;
        double dist = Math.Sin(deg2rad(lat1)) * Math.Sin(deg2rad(lat2)) + Math.Cos(deg2rad(lat1)) * Math.Cos(deg2rad(lat2)) * Math.Cos(deg2rad(theta));

        dist = Math.Acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        dist = dist * 1609.344;

        if (lon1 > lon2)
            dist = -dist;
        return (dist);
    }

    private static double GetZ(double lat1, double lon1, double lat2, double lon2)
    {
        lon1 = lon2;
        double theta = lon1 - lon2;
        double dist = Math.Sin(deg2rad(lat1)) * Math.Sin(deg2rad(lat2)) + Math.Cos(deg2rad(lat1)) * Math.Cos(deg2rad(lat2)) * Math.Cos(deg2rad(theta));

        dist = Math.Acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        dist = dist * 1609.344;

        if(lat1 > lat2)
            dist = -dist;
        return (dist);
    }

    // This function converts decimal degrees to radians
    private static double deg2rad(double deg)
    {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private static double rad2deg(double rad)
    {
        return (rad * 180 / Math.PI);
    }*/
}
