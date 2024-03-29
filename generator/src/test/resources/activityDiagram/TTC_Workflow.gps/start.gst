<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph id="TTC_Workflow" role="rule" edgeids="false" edgemode="directed">
        <node id="n0">
            <attr name="layout">
                <string>62 80 0 0</string>
            </attr>
        </node>
        <edge from="n0" to="n0">
            <attr name="label">
                <string>type:ActivityDiagram</string>
            </attr>
        </edge>
        <node id="n83">
            <attr name="layout">
                <string>382 72 0 0</string>
            </attr>
        </node>
        <edge from="n83" to="n83">
            <attr name="label">
                <string>bool:false</string>
            </attr>
        </edge>
        <edge from="n0" to="n83">
            <attr name="label">
                <string>running</string>
            </attr>
        </edge>
        <node id="n84">
            <attr name="layout">
                <string>382 152 0 0</string>
            </attr>
        </node>
        <edge from="n84" to="n84">
            <attr name="label">
                <string>string:"TTC_Workflow"</string>
            </attr>
        </edge>
        <edge from="n0" to="n84">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n1">
            <attr name="layout">
                <string>62 400 0 0</string>
            </attr>
        </node>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n85">
            <attr name="layout">
                <string>277 392 0 0</string>
            </attr>
        </node>
        <edge from="n85" to="n85">
            <attr name="label">
                <string>string:"not internal"</string>
            </attr>
        </edge>
        <edge from="n1" to="n85">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n2">
            <attr name="layout">
                <string>307 472 0 0</string>
            </attr>
        </node>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>type:BooleanValue</string>
            </attr>
        </edge>
        <node id="n86">
            <attr name="layout">
                <string>612 472 0 0</string>
            </attr>
        </node>
        <edge from="n86" to="n86">
            <attr name="label">
                <string>bool:false</string>
            </attr>
        </edge>
        <edge from="n2" to="n86">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <node id="n3">
            <attr name="layout">
                <string>62 240 0 0</string>
            </attr>
        </node>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>type:Variable</string>
            </attr>
        </edge>
        <node id="n87">
            <attr name="layout">
                <string>277 312 0 0</string>
            </attr>
        </node>
        <edge from="n87" to="n87">
            <attr name="label">
                <string>string:"internal"</string>
            </attr>
        </edge>
        <edge from="n3" to="n87">
            <attr name="label">
                <string>name</string>
            </attr>
        </edge>
        <node id="n4">
            <attr name="layout">
                <string>277 232 0 0</string>
            </attr>
        </node>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>type:BooleanValue</string>
            </attr>
        </edge>
        <node id="n88">
            <attr name="layout">
                <string>552 232 0 0</string>
            </attr>
        </node>
        <edge from="n88" to="n88">
            <attr name="label">
                <string>bool:false</string>
            </attr>
        </edge>
        <edge from="n4" to="n88">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n1" to="n2">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
        <edge from="n3" to="n4">
            <attr name="label">
                <string>value</string>
            </attr>
        </edge>
    </graph>
</gxl>
