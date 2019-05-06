package com.example.planty.Classes

import android.util.Log
import com.example.planty.Objects.Branch
import com.example.planty.Objects.Identified

class UserSearch {

    /**Searches all branch names using users text, if a branch name is found to contain user text. The branch is added
     * to the list, all branches found are returned
     * @param userTextLowerCase Users search text in lowercase
     * @param allBranches List of all branches from Firebase
     * @return returns list of Branch objects
     */
    fun searchAndCheckAllBranchNames(userTextLowerCase: String, allBranches: MutableList<Branch>): MutableList<Branch> { //Searches all the branches, if any exact matches are found, returns list of all found branches
        var foundBranchObject: MutableList<Branch> = mutableListOf()//used to hold all of the found branches to match the user search
        for (branch in allBranches){
            var branchName = branch.name.toLowerCase()
            if (DataSort().findIfDataContains(userTextLowerCase, branchName)){//If the name is found. Search the entire list, to find all objects
                foundBranchObject.add(branch)
                Log.d("SuperTest","Got to searchAndCheckAllBranchNames BranchName = $branchName, Added to List. Match found $userTextLowerCase and $branchName")

            }
        }
        Log.d("SuperTest","Got to searchAndCheckAllBranchNames foundBranchObject size = ${foundBranchObject.size}")
        return foundBranchObject
    }

    /**Compares the users previous identifications to their search text and baseID, returning the path of all matched
     * baseIDs
     * (This is done to  speed up comparisons, this is a smaller list to search, instead of the entire baseID library)
     * @param userTextLowerCase Users search text in lowercase
     * @param allUserIdentifications All user identifications
     * @param baseIds all base plant types, contained within Firebase (Taken from CloudVisionData Class)
     * @return returns baseID Firebase path as string
     */
     fun checkAllUserIdents(userTextLowerCase: String, allUserIdentifications: MutableList<Identified>, baseIds: Array<String>): String {
       var foundPath = ""
        for (ident in allUserIdentifications){
            var identName = ident.baseID.toLowerCase()
            if (DataSort().findIfDataContains(userTextLowerCase, identName)){
                foundPath = checkAllBranchBaseIDs(userTextLowerCase, baseIds)
               //Now search the branches for that baseID
                //need to find the branch from the list of branches
            }
        }
         return foundPath
    }

    /**Returns Matching baseID path, to userSearch
     * @param userTextLowerCaseUsers search text in lowercase
     * @param baseIds all base plant types, contained within Firebase (Taken from CloudVisionData Class)
     * @return returns baseID Firebase path as string
     */
   fun checkAllBranchBaseIDs(userTextLowerCase: String, baseIds: Array<String>): String { //Neeeds to be basePlants, bneed to compare the usertext to baseplant baseID. But cant save them as they are different sizes.. If i were to access firebase, it would go out of scope. So need to pass every baseIUD
        var path = ""
        for (branch in baseIds){
            if (DataSort().findIfDataContains(userTextLowerCase, branch.toLowerCase())){//If the name is found. Search the entire list, to find all objects
               path = "/basePlants/$branch"
                Log.d("SuperTest","Got to checkAllBranchBaseIDs MATCH FOUND $userTextLowerCase and $branch")
            }
        }
        Log.d("SuperTest","Got to checkAllBranchBaseIDs Path = $path")
        return path
    }
}