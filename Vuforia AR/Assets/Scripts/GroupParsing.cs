using System;
using agi = HtmlAgilityPack;
using System.Net;
using System.Text;
using System.Collections.Generic;
using UnityEngine.UI;
using UnityEngine;

public class GroupParsing : MonoBehaviour
{
    public static List<SeparationRecord> separationRecords = new List<SeparationRecord>();

    private void Start()
    {
        agi.HtmlDocument doc = new agi.HtmlDocument();
        WebClient webClient = new WebClient();
        string html;
        webClient.Encoding = Encoding.UTF8;

        // parsing 할 url
        html = webClient.DownloadString("http://www.ssu.ac.kr/web/kor/plaza_b_02");
        // html 읽기
        doc.LoadHtml(html);

        agi.HtmlNode node = doc.GetElementbyId("cmscont");
        //Console.WriteLine(node.InnerText);

        // 테이블 맨위 row parsing
        List<TitleRecord> titleRecords = new List<TitleRecord>();
        foreach (agi.HtmlNode node1 in doc.DocumentNode.SelectNodes("//div[@id='cmscont']"))
        {
            foreach (agi.HtmlNode node2 in node1.SelectNodes(".//thead"))
            {
                foreach (agi.HtmlNode node3 in node2.SelectNodes(".//th"))
                {
                    TitleRecord record = new TitleRecord();
                    record.title = node3.InnerText;
                    titleRecords.Add(record);
                }
            }
        }

        // Check correctly parsing?
        /* for (int i = 0; i < titleRecords.Count; i++) {
             Console.WriteLine(titleRecords[i].title);
         }
         */

        // 동아리 정보들 parsing
        foreach (agi.HtmlNode node1 in doc.DocumentNode.SelectNodes("//div[@id='cmscont']"))
        {
            foreach (agi.HtmlNode node2 in node1.SelectNodes(".//tbody"))
            {
                int i = 0;
                foreach (agi.HtmlNode node3 in node2.SelectNodes(".//th[@class='fir']"))
                {
                    separationRecords.Add(new SeparationRecord(node3.InnerText));
                    int num = Convert.ToInt32(node3.GetAttributeValue("rowspan", ""));
                    separationRecords[i].num = num;
                    //Console.WriteLine(separationRecords[i].separation + " " + separationRecords[i].num);
                    i++;
                }

                agi.HtmlNodeCollection node4 = node2.SelectNodes(".//tr");
                // td로 잘랐을때 55개가 나옴 그 index
                int index = 0;
                for (int j = 0; j < separationRecords.Count; j++)
                {
                    agi.HtmlNodeCollection node5;
                    for (int k = 0; k < separationRecords[j].num; k++)
                    {
                        node5 = node4[index].SelectNodes(".//td");
                        GroupRecord groupRecord = new GroupRecord();
                        groupRecord.name = node5[0].InnerText;
                        groupRecord.location = node5[1].InnerText;
                        groupRecord.information = node5[2].InnerText;
                        groupRecord.since = node5[3].InnerText;
                        groupRecord.homepage = node5[4].GetAttributeValue("href", "");
                        separationRecords[j].element.Add(groupRecord);
                        index++;
                    }
                }

            }
        }

       // ShowResult(separationRecords);

    }

    public static void ShowResult(List<SeparationRecord> p)
    {
        for (int i = 0; i < p.Count; i++)
        {
            Debug.Log("\n" + p[i].separation + "\n");
            for (int j = 0; j < p[i].num; j++)
            {
                p[i].element[j].showGroupRecord();
            }
        }
    }

    public class TitleRecord
    {
        public string title { get; set; }
    }

    public class GroupRecord
    {
        public string name;
        public string location;
        public string information;
        public string since;
        public string homepage;

        public void showGroupRecord()
        {
           // Debug.Log(name);
        }

    }

    public class SeparationRecord
    {
        public string separation;
        public int num;
        public List<GroupRecord> element = new List<GroupRecord>();

        public SeparationRecord(string s)
        {
            this.separation = s;
        }
    }
}


