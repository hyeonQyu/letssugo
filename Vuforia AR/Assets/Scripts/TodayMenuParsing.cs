//using System;
//using System.Collections.Generic;
//using System.Net;
//using System.Text;
//using UnityEngine.UI;
//using agi = HtmlAgilityPack;

//public class TodayMenuParsing
//{
//    Text menuText = null;

//    // Start is called before the first frame update
//    public string RunParsing()
//    {
//        //Text _menuText = new Text();

//        agi.HtmlDocument doc = new agi.HtmlDocument();
//        WebClient webClient = new WebClient();
//        string html;
//        webClient.Encoding = Encoding.UTF8;

//        Menu m1 = new Menu("중식1"); // 중식1
//        Menu m2 = new Menu("중식2"); // 중식2
//        Menu m3 = new Menu("중식3"); // 중식3
//        Menu d1 = new Menu("석식1"); // 석식1


//        // parsing 할 url
//        html = webClient.DownloadString("http://soongguri.com/main.php?mkey=2&w=3&l=1");
//        // html 읽기
//        doc.LoadHtml(html);
//        //debug
//       // strMenu += "\n1";

//        agi.HtmlNodeCollection node2 = new agi.HtmlNodeCollection(null);
//        // 정보쪼개기 시작
//        foreach (agi.HtmlNode node1 in doc.DocumentNode.SelectNodes("//body"))
//        {
//            agi.HtmlNodeCollection node3 = node1.SelectNodes(".//td[@style='text-align:left;border:1px dotted #b2b2b2;padding:3px 3px 3px 3px;width:140px;']");
//            Classification(m1, node3[2]);
//            Classification(m2, node3[4]);
//            Classification(m3, node3[6]);
//            Classification(d1, node3[8]);
//        }

//      //  strMenu += "\n2";
//        m1.ShowMenu();
//        m2.ShowMenu();
//        m3.ShowMenu();
//        d1.ShowMenu();
//       // strMenu += "\n3";
//        return strMenu;

//    }

//    public class Menu
//    {
//        public List<string> menu = new List<string>();
//        public string time;

//        public Menu(string s)
//        {
//            time = s;
//        }

//        public void ShowMenu()
//        {
//            strMenu += time + "\n";
//            strMenu += "\ntest";
//            for (int i = 0; i < menu.Count; i++)
//            {
//                strMenu += menu[i] + "\n";
//            }
//        }
//    }

//    public void Classification(Menu menu, agi.HtmlNode node)
//    {
//        agi.HtmlNodeCollection divide_td = node.SelectNodes(".//td");
//        agi.HtmlNodeCollection check_div = divide_td[0].SelectNodes(".//div");

//        int count = check_div.Count;

//        if (check_div == null)
//            return;
//        if (count < 2)
//        {
//            string text = check_div[0].InnerHtml;
//            text = text.Replace("<br>", "</div><div>");
//            check_div[0].InnerHtml = text;
//            agi.HtmlNodeCollection tmp = check_div[0].SelectNodes(".//div");
//            for (int i = 0; i < tmp.Count; i++)
//            {
//                menu.menu.Add(tmp[i].InnerText);
//            }

//        }
//        else
//        {
//            //menu.menu.Add(node.InnerText);
//            for (int i = 0; i < count; i++)
//            {
//                menu.menu.Add(check_div[i].InnerText);
//            }

//        }


//    }

//}