using System.Collections.Generic;
using UnityEngine;
using System;
using System.Collections;

public class ARnavigating : MonoBehaviour
{
    const double INFINITE = 99999999;

    [SerializeField]
    private GameObject divice;

    [SerializeField]
    private GameObject copyRoad;

    [SerializeField]
    private GameObject copyStart;

    [SerializeField]
    private GameObject copyEnd;

    static GameObject[] copyObjects; // Copy할 노드들

    public static int Destination; // 목적지 : index로 찾는다. // 받아옴

    private List<Node> bigNodes; // 교차로 노드, 건물 노드 포함
    private List<Edge> edgeNodes;
    private List<Adjacency> adjacencies;

    public static List<int> path; // bigNode들의 길, 비울 것(newNavigate)
    public static List<Node> realPath; // 노드들이 들어있는 실제 길, 비울 것(newNavigate)

    private bool opponent = false; // edgeNode에서 거꾸로 찾았을 때 start와 end
    public static bool isFindPath = false;

    // Start is called before the first frame update
    void Start()
    {
        // new
        bigNodes = new List<Node>();
        edgeNodes = new List<Edge>();
        path = new List<int>(bigNodes.Count);
        adjacencies = new List<Adjacency>(bigNodes.Count);
        realPath = new List<Node>();


        //BigNode initialize
        InitBuilding();
        InitCross();

        // InsertEdge 함수를 위해 미리 null값 초기화
        for (int i = 0; i < bigNodes.Count; i++)
        {
            adjacencies.Add(null);
        }

        // SmallNode initialize
        InitEdge();
        InitSmallNode();

        //// Graph initialize
        InitGraph();
    }


    // Update is called once per frame
    void Update()
    {
        if (!isFindPath)
        {
            FindPath();
            CheckNode();

        }

    }

    void CheckNode()
    {
        StartCoroutine("CheckNodeCoroutine");
    }

    void SetActiveNode(int index)
    {
        while (true)
        {
            copyObjects[index].SetActive(true);
            index++;
        }
    }

    IEnumerator CheckNodeCoroutine()
    {
        int myIndex = 0, currentIndex = 0;
        double min_distance = INFINITE;
        while (ButtonListeners.IsNavigating)
        {
            myIndex = currentIndex;
            min_distance = Distance(divice, realPath[myIndex]);
            if (myIndex != 0 && myIndex != realPath.Count - 1)
            {

                if (min_distance > Distance(divice, realPath[myIndex - 1]))
                {
                    currentIndex = myIndex - 1;
                }
                else if (min_distance > Distance(divice, realPath[myIndex + 1]))
                {
                    currentIndex = myIndex + 1;
                }
            }
            else if (myIndex == 0)
            {
                if (min_distance > Distance(divice, realPath[myIndex + 1]))
                {
                    currentIndex = myIndex + 1;
                }
            }

            if (myIndex != currentIndex)
            {
                if (currentIndex >= 2 && currentIndex <= realPath.Count - 6)
                {
                    ShutDown();

                    for (int i = currentIndex - 2; i <= currentIndex + 5; i++)
                    {
                        copyObjects[i].SetActive(true);
                    }

                }
            }
            yield return new WaitForSeconds(0.01f);
        }
    }

    void ShutDown()
    {
        for (int i = 0; i < realPath.Count; i++)
        {
            copyObjects[i].SetActive(false);
        }
    }

    void FindPath()
    {
        // 다이스트라를 이용한 BigNode Path 추적
        TracePath(Dijkstra(CloseBigNodeIndex(), Destination), Destination);

        // RealPath 구하기
        InsertRealPath();

        copyObjects = new GameObject[realPath.Count];

        // CopyNodes 생성(초기 setActive(false))
        CreateNode();

        isFindPath = true;
    }

    void CreateNode()
    {
        copyObjects[0] = Instantiate(copyStart, new Vector3(realPath[0].x, realPath[0].y, realPath[0].z), Quaternion.identity);
        for (int i = 1; i < realPath.Count - 1; i++)
        {
            copyObjects[i] = Instantiate(copyRoad, new Vector3(realPath[i].x, realPath[i].y, realPath[i].z), Quaternion.identity);

        }
        copyObjects[realPath.Count - 1] = Instantiate(copyEnd, new Vector3(realPath[realPath.Count - 1].x, realPath[realPath.Count - 1].y, realPath[realPath.Count - 1].z), Quaternion.identity);

        if (realPath.Count > 7)
        {
            for (int i = 6; i < realPath.Count; i++)
            {
                copyObjects[i].SetActive(false);
            }
        }
    }

    int CloseBigNodeIndex()
    {
        double min = INFINITE;
        int meMinIndex = 0;
        for (int i = 0; i < bigNodes.Count; i++)
        {
            if (min > Distance(divice, bigNodes[i]))
            {
                min = Distance(divice, bigNodes[i]);
                meMinIndex = i;
            }
        }

        return meMinIndex;
    }

    void InitBuilding()
    {
        // Building initialize
        bigNodes.Add(new Node(542f, 10f, 499f, "B_0")); // 0
        bigNodes.Add(new Node(580f, 10f, 577f, "B_1")); // 1
        bigNodes.Add(new Node(643f, 10f, 609f, "B_2")); // 2
        bigNodes.Add(new Node(708.4f, 10f, 586f, "B_3")); // 3
        bigNodes.Add(new Node(769f, 10f, 451f, "B_4")); // 4
        bigNodes.Add(new Node(700f, 10f, 354.6f, "B_5")); // 5
    }

    void InitCross()
    {
        // Cross streat initialize
        bigNodes.Add(new Node(561.5f, 10f, 495.8f, "C_6")); // 6
        bigNodes.Add(new Node(572.1f, 10f, 530f, "C_7")); // 7
        bigNodes.Add(new Node(624f, 10f, 564f, "C_8")); // 8
        bigNodes.Add(new Node(631.1f, 10f, 609f, "C_9")); // 9
        bigNodes.Add(new Node(705.2f, 10f, 564.1f, "C_10")); // 10
        bigNodes.Add(new Node(758.9f, 10f, 535.6f, "C_11")); // 11
        bigNodes.Add(new Node(750.9f, 10f, 455.3f, "C_12")); // 12
        bigNodes.Add(new Node(736.6f, 10f, 419f, "C_13")); // 13
        bigNodes.Add(new Node(697.5f, 10f, 378.3f, "C_14")); // 14
        bigNodes.Add(new Node(612.3f, 10f, 378f, "C_15")); // 15
        bigNodes.Add(new Node(611f, 10f, 432f, "C_16")); // 16
        bigNodes.Add(new Node(566f, 10f, 437f, "C_17")); // 17
    }

    void InitEdge()
    {
        // Edge Node initialize
        edgeNodes.Add(new Edge(bigNodes[0], bigNodes[6])); // 0 (0-6)
        edgeNodes.Add(new Edge(bigNodes[1], bigNodes[7])); // 1 (1-7)
        edgeNodes.Add(new Edge(bigNodes[1], bigNodes[8])); // 2 (1-8)
        edgeNodes.Add(new Edge(bigNodes[2], bigNodes[9])); // 3 (2-9)
        edgeNodes.Add(new Edge(bigNodes[3], bigNodes[10])); // 4 (3-10)
        edgeNodes.Add(new Edge(bigNodes[4], bigNodes[12])); // 5 (4-12)
        edgeNodes.Add(new Edge(bigNodes[5], bigNodes[14])); // 6 (5-14)

        edgeNodes.Add(new Edge(bigNodes[6], bigNodes[7])); // 7 (6-7)
        edgeNodes.Add(new Edge(bigNodes[6], bigNodes[17])); // 8 (6-17)
        edgeNodes.Add(new Edge(bigNodes[7], bigNodes[8])); // 9 (7-8)
        edgeNodes.Add(new Edge(bigNodes[8], bigNodes[9])); // 10 (8-9)
        edgeNodes.Add(new Edge(bigNodes[8], bigNodes[10])); // 11 (8-10)
        edgeNodes.Add(new Edge(bigNodes[10], bigNodes[11])); // 12 (10-11)
        edgeNodes.Add(new Edge(bigNodes[11], bigNodes[12])); // 13 (11-12)
        edgeNodes.Add(new Edge(bigNodes[12], bigNodes[13])); // 14 (12-13)
        edgeNodes.Add(new Edge(bigNodes[13], bigNodes[14])); // 15 (13-14)
        edgeNodes.Add(new Edge(bigNodes[13], bigNodes[16])); // 16 (13-16)
        edgeNodes.Add(new Edge(bigNodes[14], bigNodes[15])); // 17 (14-15)
        edgeNodes.Add(new Edge(bigNodes[15], bigNodes[16])); // 18 (15-16)
        edgeNodes.Add(new Edge(bigNodes[16], bigNodes[17])); // 19 (16-17)
    }

    void InitSmallNode()
    {
        //smallNodes initialize
        edgeNodes[1].smallNodes.Add(new Node(574.8f, 10f, 552.6f, null)); // 1-7
        edgeNodes[2].smallNodes.Add(new Node(600.1f, 10f, 566.2f, null)); // 1-8
        edgeNodes[7].smallNodes.Add(new Node(569.4f, 10f, 511.7f, null)); // 6-7
        edgeNodes[8].smallNodes.Add(new Node(567f, 10f, 475.5f, null)); // 6-17
        edgeNodes[8].smallNodes.Add(new Node(567f, 10f, 457.4f, null)); // 6-17
        edgeNodes[9].smallNodes.Add(new Node(592f, 10f, 530f, null)); // 7-8
        edgeNodes[9].smallNodes.Add(new Node(607f, 10f, 538f, null)); // 7-8
        edgeNodes[9].smallNodes.Add(new Node(619f, 10f, 549f, null)); // 7-8
        edgeNodes[10].smallNodes.Add(new Node(623.6f, 10f, 585f, null)); // 8-9
        edgeNodes[11].smallNodes.Add(new Node(642f, 10f, 561f, null)); // 8-10
        edgeNodes[11].smallNodes.Add(new Node(661f, 10f, 557f, null)); // 8-10
        edgeNodes[11].smallNodes.Add(new Node(682f, 10f, 560.2f, null)); // 8-10
        edgeNodes[12].smallNodes.Add(new Node(724f, 10f, 552.5f, null)); // 10-11
        edgeNodes[12].smallNodes.Add(new Node(742f, 10f, 543.3f, null)); // 10-11
        edgeNodes[13].smallNodes.Add(new Node(754.3f, 10f, 518.5f, null)); // 11-12
        edgeNodes[13].smallNodes.Add(new Node(752.8f, 10f, 494.5f, null)); // 11-12
        edgeNodes[13].smallNodes.Add(new Node(751.3f, 10f, 476f, null)); // 11-12
        edgeNodes[14].smallNodes.Add(new Node(743.9f, 10f, 438.7f, null)); // 12-13
        edgeNodes[15].smallNodes.Add(new Node(726.6f, 10f, 404.2f, null)); // 13-14
        edgeNodes[15].smallNodes.Add(new Node(710.7f, 10f, 388.6f, null)); // 13-14
        edgeNodes[16].smallNodes.Add(new Node(709f, 10f, 424f, null)); // 13-16
        edgeNodes[16].smallNodes.Add(new Node(679f, 10f, 427f, null)); // 13-16
        edgeNodes[16].smallNodes.Add(new Node(654f, 10f, 429f, null)); // 13-16
        edgeNodes[16].smallNodes.Add(new Node(630f, 10f, 431f, null)); // 13-16
        edgeNodes[17].smallNodes.Add(new Node(675.2f, 10f, 382.6f, null)); // 14-15
        edgeNodes[17].smallNodes.Add(new Node(652.6f, 10f, 382.8f, null)); // 14-15
        edgeNodes[17].smallNodes.Add(new Node(630.9f, 10f, 380.6f, null)); // 14-15
        edgeNodes[18].smallNodes.Add(new Node(612.2f, 10f, 398.4f, null)); // 15-16
        edgeNodes[18].smallNodes.Add(new Node(612.2f, 10f, 415.7f, null)); // 15-16
        edgeNodes[19].smallNodes.Add(new Node(589f, 10f, 436f, null)); // 16-17
    }

    void InitGraph()
    {
        //Insert Edge
        InsertEdge(0, 6, Distance(bigNodes[0], bigNodes[6]));
        InsertEdge(1, 7, Distance(bigNodes[1], bigNodes[7]));
        InsertEdge(1, 8, Distance(bigNodes[1], bigNodes[8]));
        InsertEdge(2, 9, Distance(bigNodes[2], bigNodes[9]));
        InsertEdge(3, 10, Distance(bigNodes[3], bigNodes[10]));
        InsertEdge(4, 12, Distance(bigNodes[4], bigNodes[12]));
        InsertEdge(5, 14, Distance(bigNodes[5], bigNodes[14]));
        InsertEdge(6, 7, Distance(bigNodes[6], bigNodes[7]));
        InsertEdge(6, 17, Distance(bigNodes[6], bigNodes[17]));
        InsertEdge(7, 8, Distance(bigNodes[7], bigNodes[8]));
        InsertEdge(8, 9, Distance(bigNodes[8], bigNodes[9]));
        InsertEdge(8, 10, Distance(bigNodes[8], bigNodes[10]));
        InsertEdge(10, 11, Distance(bigNodes[10], bigNodes[11]));
        InsertEdge(11, 12, Distance(bigNodes[11], bigNodes[12]));
        InsertEdge(12, 13, Distance(bigNodes[12], bigNodes[13]));
        InsertEdge(13, 14, Distance(bigNodes[13], bigNodes[14]));
        InsertEdge(13, 16, Distance(bigNodes[13], bigNodes[16]));
        InsertEdge(14, 15, Distance(bigNodes[14], bigNodes[15]));
        InsertEdge(15, 16, Distance(bigNodes[15], bigNodes[16]));
        InsertEdge(16, 17, Distance(bigNodes[16], bigNodes[17]));

    }
    double Distance(Node bigNode1, Node bigNode2)
    {
        return Math.Sqrt(Math.Pow((bigNode1.x - bigNode2.x), 2) + Math.Pow((bigNode1.y - bigNode2.y), 2) + Math.Pow((bigNode1.z - bigNode2.z), 2));
    }

    double Distance(GameObject obj, Node bigNode1)
    {
        return Math.Sqrt(Math.Pow((obj.transform.position.x - bigNode1.x), 2) + Math.Pow((obj.transform.position.y - bigNode1.y), 2) + Math.Pow((obj.transform.position.z - bigNode1.z), 2));
    }


    void InsertEdge(int startIndex, int endIndex, double weight)
    {
        Adjacency node1 = new Adjacency();
        Adjacency node2 = new Adjacency();

        node1.bigNodeIndex = endIndex;
        node1.weight = weight;
        node1.link = adjacencies[startIndex];

        adjacencies[startIndex] = node1;

        node2.bigNodeIndex = startIndex;
        node2.weight = weight;
        node2.link = adjacencies[endIndex];

        adjacencies[endIndex] = node2;
    }

    int[] Dijkstra(int start, int end)
    {
        int vCnt = bigNodes.Count; // 정점의 개수
        double[] distance = new double[vCnt]; // 거리
        int[] pathCnt = new int[vCnt];  // 경로 추적할 때 필요
        int[] check = new int[vCnt]; // 가방 안에 있는지 여부

        Adjacency temp = null;

        double min;
        int cycle, now;  // cycle : 작업 횟수 now : 현재 정점

        // Initialize
        for (int i = 0; i < vCnt; i++)
        {
            distance[i] = INFINITE;
            pathCnt[i] = -1;
            check[i] = 0;
        }
        distance[start] = 0;
        cycle = 0;
        now = 0;

        while (cycle < vCnt - 1) // 모든 정점에 대해 하기 위해서
        {
            min = INFINITE; // 최소값 무한으로 초기화
            for (int i = 0; i < vCnt; i++)
            {
                if (distance[i] < min && check[i] == 0) // 가방 밖의 정점 중 거리가 최소인 정점으로부터 시작
                {
                    min = distance[i];
                    now = i; // 가방에 넣을 정점 위치
                }
            }
            check[now] = 1; // 가방 안에 넣기
            temp = adjacencies[now]; // 가방에 새로 들어온 정점의 구조체 포인터

            while (temp != null)
            {
                if (check[temp.bigNodeIndex] == 0) // 가방 밖의 정점이라면
                {
                    //  수행한 거리 < 기존 최단거리 --> 간선 완화
                    if (min + temp.weight < distance[temp.bigNodeIndex])
                    {
                        distance[temp.bigNodeIndex] = min + temp.weight; // 최단거리 갱신

                        // 만약 시작점에서의 연결점이라면 경로수 start
                        if (now == start)
                        {
                            pathCnt[temp.bigNodeIndex] = start;
                        }

                        // 그 이외에는 부모위치의 경로수
                        else
                        {
                            pathCnt[temp.bigNodeIndex] = now;
                        }
                    }
                }
                temp = temp.link; // 다음 인접 정점, 간선 검사
            }
            cycle++;
            if (now == end) { break; } // 현재 정점이 도착 정점과 같다면 종료
        }

        return pathCnt;
    }

    void TracePath(int[] pathCnt, int end)
    {
        int i = end;
        while (pathCnt[i] != -1)
        {
            i = pathCnt[i];
            path.Add(i);
        }
        path.Insert(0, Destination);
        path.Reverse();
    }

    int EdgeFindIndex(int pathIndex)
    {
        int i = 0;

        while (i < edgeNodes.Count)
        {
            if (edgeNodes[i].compareStartEnd(bigNodes[path[pathIndex]], bigNodes[path[pathIndex + 1]]))
            {
                break;
            }
            if (edgeNodes[i].compareStartEnd(bigNodes[path[pathIndex + 1]], bigNodes[path[pathIndex]]))
            {
                opponent = true;
                break;
            }
            i++;
        }

        return i;
    }

    void InsertRealPath()
    {
        // 처음 edge만 내 위치와 비교(edge안에 내가 있을 수 있으므로)
        int edgeNodeIndex = EdgeFindIndex(0); // 처음 edgeNode index

        // 처음 bigNode와 내 위치와의 거리
        double min = Distance(divice, bigNodes[path[0]]);
        int smallNodeIndex = -1; // smallNode와 가까울 때 smallNode의 index
        double temp = INFINITE;

        // smallNode와 가까운지 점검
        for (int i = 0; i < edgeNodes[edgeNodeIndex].smallNodes.Count; i++)
        {
            temp = Distance(divice, edgeNodes[edgeNodeIndex].smallNodes[i]);
            if (min > temp)
            {
                min = temp;
                smallNodeIndex = i;
            }
        }
        if (smallNodeIndex == -1) // bigNode가 제일 가까울 때
        {
            realPath.Add(bigNodes[path[0]]);
            if (!opponent) // edgeNode 찾을 때 올바르게 찾았다면
            {
                for (int i = 0; i < edgeNodes[edgeNodeIndex].smallNodes.Count; i++)
                {
                    realPath.Add(edgeNodes[edgeNodeIndex].smallNodes[i]);
                }
            }
            else // edgeNode 찾을 때 거꾸로 찾았다면
            {
                for (int i = edgeNodes[edgeNodeIndex].smallNodes.Count - 1; i >= 0; i--)
                {
                    realPath.Add(edgeNodes[edgeNodeIndex].smallNodes[i]);
                }
                opponent = false;
            }
        }
        else // edge안에 내가 있어서 smallNode와 가까울 때
        {
            if (!opponent) // edgeNode 찾을 때 올바르게 찾았다면
            {
                for (int i = smallNodeIndex; i < edgeNodes[edgeNodeIndex].smallNodes.Count; i++)
                {
                    realPath.Add(edgeNodes[edgeNodeIndex].smallNodes[i]);
                }
            }
            else // edgeNode 찾을 때 거꾸로 찾았다면
            {
                for (int i = smallNodeIndex; i >= 0; i--)
                {
                    realPath.Add(edgeNodes[edgeNodeIndex].smallNodes[i]);
                }
                opponent = false;
            }
        }

        /////////////////////////////////////////////////////////////////
        // 그 다음 bigNode부터
        for (int i = 1; i < path.Count - 1; i++)
        {
            realPath.Add(bigNodes[path[i]]);
            edgeNodeIndex = EdgeFindIndex(i);
            if (!opponent)
            { // 올바를 때
                for (int j = 0; j < edgeNodes[edgeNodeIndex].smallNodes.Count; j++)
                {
                    realPath.Add(edgeNodes[edgeNodeIndex].smallNodes[j]);
                }
            }
            else // 거꾸로일 때
            {
                for (int j = edgeNodes[edgeNodeIndex].smallNodes.Count - 1; j >= 0; j--)
                {
                    realPath.Add(edgeNodes[edgeNodeIndex].smallNodes[j]);
                }
                opponent = false;
            }
        }
        realPath.Add(bigNodes[Destination]); // 도착지 노드 추가 or 도착지 노드에 다른 오브젝트?
    }

    public static void ClearNavi()
    {
        // Destroy
        for (int i = 0; i < realPath.Count; i++)
        {
            Destroy(copyObjects[i]);
        }
        path.Clear();
        realPath.Clear();
        isFindPath = false;
    }
}