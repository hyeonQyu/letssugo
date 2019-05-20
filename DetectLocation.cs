using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Kudan.AR.Samples;
using UnityEngine.UI;

public class DetectLocation : MonoBehaviour
{
    // location marker show
    private Vector2 _targetCoordinates;
    // device location var
    private Vector2 _deviceCoordinates;
    // distance allowed fro device to target
    private float _distanceFromTarget = 0.00004f;
    // distance between device to target coordinates
    private float _proximity = 0.001f;
    // values for latitude and longtitude get from device gps
    private float _sLatitude, _sLongtitude;
    // target location 37.494480, 126.959799
    [SerializeField]
    private float _dLatitude = 37.494480f;
    [SerializeField]
    private float _dLongtitude = 126.959799f;
    public float DLatitude
    {
        get { return _dLatitude; }
        set { _dLatitude = value; }
    }
    public float DLongtitude
    {
        get { return _dLongtitude; }
        set { _dLongtitude = value; }
    }
    // var for location request
    private bool _enableByRequest = true;
    [SerializeField]
    private int _maxWait = 10;
    [SerializeField]
    private bool _isReady = false;
    [SerializeField]
    private Text _text;
    // sample app script
    [SerializeField]
    private SampleApp _sampleApp;

    private int n;

    void Start()
    {
        n = 0;
        _targetCoordinates = new Vector2(_dLatitude, _dLongtitude);
       // _sLatitude = _dLatitude;
        //_sLongtitude = _dLongtitude;
        StartCoroutine(GetLocation());
    }

    void Update()
    {
        StartCalculate();

        _text.text = "Target Location : " + _dLatitude + ", " + _dLongtitude + "\nMy Location: " + _sLatitude + ", " + _sLongtitude;

        if(n < 30)
        {
            n++;
            _sLatitude += 0.00001f;
            _sLongtitude += 0.00001f;
        }
        else
        {
            n = 0;
            _sLatitude = _dLatitude;
            _sLongtitude = _dLongtitude;
        }
    }
    
    // get last update location, we need latitude and longtitude
    IEnumerator GetLocation()
    {
        LocationService service = Input.location;
        if(!_enableByRequest && !service.isEnabledByUser)
        {
            Debug.Log("Location Services not enabled by user");
            yield break;
        }
        service.Start();
        while(service.status == LocationServiceStatus.Initializing && _maxWait > 0)
        {
            yield return new WaitForSeconds(1);
            _maxWait--;
        }
        if(_maxWait < 1)
        {
            Debug.Log("timed out");
            yield break;
        }
        if(service.status == LocationServiceStatus.Failed)
        {
            Debug.Log("Unable to determine device location");
            yield break;
        }
        else
        {
            _text.text = "Target Location : " + _dLatitude + ", " + _dLongtitude + "\nMy Location: " + service.lastData.latitude + ", " + service.lastData.longitude;
            _sLatitude = service.lastData.latitude;
            _sLongtitude = service.lastData.longitude;
        }
        // service.Stop();
        _isReady = true;
        StartCalculate();
    }

    // method to calculate distances between device location and target location
    public void StartCalculate()
    {
        _deviceCoordinates = new Vector2(_sLatitude, _sLongtitude);
        _proximity = Vector2.Distance(_targetCoordinates, _deviceCoordinates);

        if(_proximity <= _distanceFromTarget)
        {
            _text.text = _text.text + "\nDistance : " + _proximity.ToString();
            _text.text += "\nTarget Detected";

            _sampleApp.StartClicked();
        }
        else
        {
            _text.text = _text.text + "\nDistance : " + _proximity.ToString();
            _text.text += "\nTarget not detected, too far!";
        }
    }
}