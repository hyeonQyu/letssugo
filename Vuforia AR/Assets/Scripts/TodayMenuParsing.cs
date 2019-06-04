using System;
using System.Collections;
using System.Collections.Generic;
using System.Net;
using System.Text;
using UnityEngine;
using UnityEngine.UI;
using agi = HtmlAgilityPack;

public class TodayMenuParsing:MonoBehaviour
{
    [SerializeField]
    Text _menuText;

    // Start is called before the first frame update
    void Start()
    {
        _menuText.text = null;
        agi.HtmlDocument doc = new agi.HtmlDocument();
        WebClient webClient = new WebClient();
        string html;
        webClient.Encoding = Encoding.UTF8;

        Menu m1 = new Menu("중식1"); // 중식1
        Menu m2 = new Menu("중식2"); // 중식2
        Menu m3 = new Menu("중식3"); // 중식3
        Menu d1 = new Menu("석식1"); // 석식1


        // parsing 할 url
        html = webClient.DownloadString("http://soongguri.com/main.php?mkey=2&w=3&l=1");
        // html 읽기
        doc.LoadHtml(html);

        agi.HtmlNodeCollection node2 = new agi.HtmlNodeCollection(null);
        // 정보쪼개기 시작
        foreach(agi.HtmlNode node1 in doc.DocumentNode.SelectNodes("//body"))
        {
            agi.HtmlNodeCollection node3 = node1.SelectNodes(".//td[@style='text-align:left;border:1px dotted #b2b2b2;padding:3px 3px 3px 3px;width:140px;']");
            Classification(m1, node3[2]);
            Classification(m2, node3[4]);
            Classification(m3, node3[6]);
            Classification(d1, node3[8]);
        }

        m1.ShowMenu(_menuText);
        m2.ShowMenu(_menuText);
        m3.ShowMenu(_menuText);
        d1.ShowMenu(_menuText);
    }

    public class Menu
    {
        public List<string> menu = new List<string>();
        public String time;

        public Menu(String s)
        {
            time = s;
        }

        public void ShowMenu(Text text)
        {
            text.text = text.text + "\n" + time + "\n";
            int count = menu.Count;
            for(int i = 0; i < count; i++)
            {
                if(menu[i] == "")
                    continue;
                if(i == count - 2)
                    text.text = text.text + menu[i] + " ";
                else
                    text.text = text.text + menu[i] + "\n";
            }
        }
    }

    public static void Classification(Menu menu, agi.HtmlNode node)
    {
        agi.HtmlNodeCollection divide_td = node.SelectNodes(".//td");
        agi.HtmlNodeCollection check_div = divide_td[0].SelectNodes(".//div");
        agi.HtmlNodeCollection check_br = divide_td[0].SelectNodes(".//br");

        int count = check_br.Count;

        if(check_div == null)
            return;
        if(count > 2)
        {
            String text = divide_td[0].InnerHtml;
            text = text.Replace("<br>", "</div><div>");
            divide_td[0].InnerHtml = text;
            agi.HtmlNodeCollection tmp = divide_td[0].SelectNodes(".//div");
            for(int i = 0; i < tmp.Count; i++)
            {
                menu.menu.Add(tmp[i].InnerText);
            }

        }
        else
        {
            //menu.menu.Add(node.InnerText);
            for(int i = 0; i < count; i++)
            {
                menu.menu.Add(check_div[i].InnerText);
            }

        }


    }

}