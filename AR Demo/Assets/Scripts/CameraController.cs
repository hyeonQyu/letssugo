using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class CameraController : MonoBehaviour
{
    public GameObject Cube;

    [SerializeField]
    private Text _excuteNum;
    [SerializeField]
    private Text _txtLatitude;
    [SerializeField]
    private Text _txtLongitude;

    private float _myLatitude;
    private float _myLongitude;
    private float _targetLatitude;
    private float _targetLongitude;

    private double _distance;

    private int _num;

    // Start is called before the first frame update
    void Start()
    {
        Cube.SetActive(false);
        SetTargetLocation(37.494391f, 126.960081f);
        _num = 0;
        //  Input.compass.enabled = true;
        CheckGPSConnection();
    }

    // Update is called once per frame
    void Update()
    {
        ConnectToGPS();
        CheckDistance();
    }

    private void CheckGPSConnection()
    {
        if(!Input.location.isEnabledByUser)     // GPS 설정 안되어있음
            _excuteNum.text = "Can't Use GPS";
        else
            _excuteNum.text = "is good";
    }

    private void ConnectToGPS()
    {
        StartCoroutine(CorConnectToGPS());
    }

    IEnumerator CorConnectToGPS()
    {
        if(!Input.location.isEnabledByUser)
            yield break;

        Input.location.Start();     // 위치정보를 받기 시작
        Input.compass.enabled = true;
        //int maxWait = 20;
        while(Input.location.status == LocationServiceStatus.Initializing/* && maxWait > 0*/)
        {
            _myLatitude = Input.location.lastData.latitude;
            _myLongitude = Input.location.lastData.longitude;
            _txtLatitude.text = "latitude" + Input.location.lastData.latitude;
            _txtLongitude.text = "longtitude" + Input.location.lastData.longitude;
            _num++;
            _excuteNum.text = "Num = " + _num;
            yield return new WaitForSeconds(100);
        }
    }

    private void SetTargetLocation(float latitude, float longitude)     // 타겟좌표 지정
    {
        _targetLatitude = latitude;
        _targetLongitude = longitude;
    }

    private static double GetDistance(double lat1, double lon1, double lat2, double lon2)      // 타겟위치와 현재위치의 거리 차이
    {
        double theta = lon1 - lon2;
        double dist = Math.Sin(Deg2rad(lat1)) * Math.Sin(Deg2rad(lat2)) + Math.Cos(Deg2rad(lat1)) * Math.Cos(Deg2rad(lat2)) * Math.Cos(Deg2rad(theta));

        dist = Math.Acos(dist);
        dist = Rad2deg(dist);
        dist = dist * 60 * 1.1515;

        dist = dist * 1609.344;

        return (dist);
    }

    private static double Deg2rad(double deg)
    {
        return (deg * Math.PI / 180.0);
    }

    private static double Rad2deg(double rad)
    {
        return (rad * 180 / Math.PI);
    }

    private void CheckDistance()
    {
        _distance = GetDistance(_targetLatitude, _targetLongitude, _myLatitude, _myLongitude);
        if(_distance < 10)
            Cube.SetActive(true);
        else
            Cube.SetActive(false);
    }
}
