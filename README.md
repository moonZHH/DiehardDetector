# DiehardDetector: Detecting Diehard Android Apps

Many apps (diehard apps) employ approaches (diehard methods) to keep themselves alive or wake up themselves (or other apps). DiehardDetector is a static analysis tools for identifying diehard apps.

If you are interested about the details of DiehardDetect, please refer to our research paper:  

@inproceedings {dieharddetector20,  
　title = {Demystifying diehard android apps},  
　author = {Zhou, Hao and Wang, Haoyu and Zhou, Yajin and Luo, Xiapu and Tang, Yutian and Xue, Lei and Wang, Ting},  
　booktitle = {2020 35th IEEE/ACM International Conference on Automated Software Engineering (ASE)},  
　pages = {187--198},  
　year = {2020},  
　organization = {IEEE}  
}

To use DiehardDetector, please follow the steps:  
　Step1: Import the folders appalive and FlowDroid_new to Eclipse.  
　Step2: Adjust the configurations in Config.java.  
　Step3: Run Main.java to perform analysis.  
