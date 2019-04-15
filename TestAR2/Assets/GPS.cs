using UnityEngine;
using UnityEngine.UI;
using System.Collections;
using System.Collections.Generic;
using System;

public class GPS : MonoBehaviour
{

    
    LocationInfo currentGPSPosition;

    public GameObject ufo;
    public Text coordinates;
    public Text referenceCoordinates;
    public Text distanceFrom3dModel;

    private double referenceLatitude;
    private double referenceLongitude;
    private double referenceDistance;

    private double distFrom3dModel_lat;
    private double distFrom3dModel_lon;

    private void Start()
    {
        DontDestroyOnLoad(gameObject);
        ufo.SetActive(false);
        StartLocationService();
        referenceLatitude = 37.494720;
        referenceLongitude = 126.960140;
        referenceDistance = 0.00020;
        distFrom3dModel_lat = 0;
        distFrom3dModel_lon = 0;
        distanceFrom3dModel.text = "Distance (Lan,Lon): 0 - 0 ";
        referenceCoordinates.text = "Reference (Lat,Lon): "+referenceLatitude.ToString()+" - "+referenceLongitude.ToString();
    }

    private void Update()
    {
        currentGPSPosition = Input.location.lastData;

        coordinates.text = "Current (Lat,Lon): " +currentGPSPosition.latitude.ToString() + " - "+currentGPSPosition.longitude.ToString();
        if (CloseEnoughForMe(currentGPSPosition.latitude, referenceLatitude, referenceDistance) && CloseEnoughForMe(currentGPSPosition.longitude, referenceLongitude, referenceDistance))
        {
            ufo.SetActive(true);
        }
        else
        {
            ufo.SetActive(false);
        }
    }

    private bool CloseEnoughForMe(double value1, double value2, double acceptableDifference)
    {
        distanceFrom3dModel.text = "Distance (Lan,Lon): " + Math.Abs(value1 - value2).ToString();
        return Math.Abs(value1 - value2) <= acceptableDifference;
    }

    private double distanceBetweenTwoPoints(double value1, double value2)
    {
        return Math.Abs(value1 - value2);
    }

    private void StartLocationService()
    {
        Input.location.Start(0.5f);

        int wait = 1000;

        if(Input.location.status == LocationServiceStatus.Initializing && wait > 0)
        {
            while(Input.location.isEnabledByUser)
            {
                wait--;
            }

            if(Input.location.status == LocationServiceStatus.Failed)
            {

            }
        }
        else
        {
            coordinates.text = "GPS not available";
        }
    }
}